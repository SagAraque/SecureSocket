package com.keyvault;

import org.apache.commons.lang3.SerializationUtils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.zip.*;

public class SecureSocket {
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private BufferedInputStream bufferedInputStream;
    private BufferedOutputStream bufferedOutputStream;
    private Cipher cipher;
    private Mac hmac;
    private String cipher_suite = "AES";
    private String hmacAlgorithm = "HmacSHA256";
    private SecretKey secretKey;
    private boolean isServer = false;

    public SecureSocket(String host, int port) throws IOException
    {
        socket = new Socket(host, port);
        openStreams();
        initNegotiation();
    }

    public SecureSocket(Socket socket) throws IOException
    {
        this.socket = socket;
        isServer = true;
        openStreams();
        initNegotiation();
    }

    private void openStreams() throws IOException {
        bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
        dataOutputStream = new DataOutputStream(bufferedOutputStream);
        bufferedInputStream = new BufferedInputStream(socket.getInputStream());
        dataInputStream = new DataInputStream(bufferedInputStream);
    }

    public void close() throws IOException
    {
        dataOutputStream.close();
        bufferedOutputStream.close();
        dataInputStream.close();
        bufferedInputStream.close();
        socket.close();
    }

    public void initNegotiation()
    {
        try
        {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DiffieHellman");
            keyPairGenerator.initialize(2048);
            KeyPair key = keyPairGenerator.generateKeyPair();

            byte[] remoteKey;

            if(isServer)
            {
                writeData(key.getPublic().getEncoded());
                remoteKey = readData();
            }
            else
            {
                remoteKey = readData();
                writeData(key.getPublic().getEncoded());
            }


            KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
            keyAgreement.init(key.getPrivate());
            keyAgreement.doPhase(
                    KeyFactory.getInstance("DH").generatePublic(new X509EncodedKeySpec(remoteKey)), true
            );

            secretKey = new SecretKeySpec(Arrays.copyOfRange(keyAgreement.generateSecret(), 0, 32), cipher_suite);

            cipher = Cipher.getInstance(cipher_suite);

            hmac = Mac.getInstance(hmacAlgorithm);
            hmac.init(secretKey);
        }
        catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IOException |
               InvalidKeySpecException e)
        {
            e.printStackTrace();
        }

    }

    private byte[] readData() throws IOException
    {
        int length = dataInputStream.readInt();
        byte[] buffer = new byte[length];
        dataInputStream.readFully(buffer);

        return buffer;
    }

    private void writeData(byte[]... dataList) throws IOException
    {
        for (byte[] data : dataList) {
            dataOutputStream.writeInt(data.length);
            dataOutputStream.flush();
            bufferedOutputStream.flush();
            dataOutputStream.write(data);
            dataOutputStream.flush();
            bufferedOutputStream.flush();
        }
    }

    public void writeObject(Object object) throws IllegalBlockSizeException, BadPaddingException, IOException, InvalidKeyException
    {
        byte[] serializedObject = SerializationUtils.serialize((Serializable) object);
        generateMessage(serializedObject);
    }

    public void writeUTF(String data) throws IOException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException
    {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        generateMessage(dataBytes);
    }

    private void generateMessage(byte[] data) throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
    {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedData = cipher.doFinal(data);
        byte[] hash = hmac.doFinal(encryptedData);

        writeData(encryptedData, hash);
    }

    private byte[] read() throws IOException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, DataFormatException
    {
        byte[] data = readData();
        byte[] hash = readData();
        byte[] responseHash = hmac.doFinal(data);

        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        if(Arrays.equals(hash, responseHash))
        {
            return cipher.doFinal(data);
        }
        else
        {
            close();
            return null;
        }
    }

    public Object readObject() throws Exception
    {
        byte[] data = read();

        if(data != null)
            return SerializationUtils.deserialize(data);

        return null;
    }

    public String readUTF() throws IllegalBlockSizeException, IOException, BadPaddingException, InvalidKeyException, DataFormatException
    {
        byte[] data = read();
        String dataUTF = null;

        if(data != null)
            dataUTF = new String(data, StandardCharsets.UTF_8);

        return dataUTF;
    }

    public String getHost()
    {
        return socket.getInetAddress().getHostAddress();
    }

}
