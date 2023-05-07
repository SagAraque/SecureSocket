import com.keyvault.SecureSocket;

import java.net.ServerSocket;

public class TestServer {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(2222);

        while (true)
        {
            SecureSocket secureSocket = new SecureSocket(serverSocket.accept());

            Request r = new Request(new Object[]{new Request()},Request.GET);

            secureSocket.writeObject(r);
            System.out.println(secureSocket.readObject());
        }
    }
}
