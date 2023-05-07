import com.keyvault.SecureSocket;

import java.nio.charset.StandardCharsets;
public class TestClient {
    public static void main(String[] args) throws Exception {
        SecureSocket secureSocket = new SecureSocket("localhost", 2222);

        Request r = (Request) secureSocket.readObject();
        System.out.println(r.getOperationCode());
        System.out.println(r.getContent()[0].getClass());

        byte[] a = "MondongonExtremoAbueMondongonExtnE".getBytes(StandardCharsets.UTF_8);
        secureSocket.writeObject(new Request());
    }
}
