import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ReceiveThread extends Thread {

    private Socket socket;
    private BufferedReader reader;
    private InputStream inputStream;
    private String line;
    private Client client;


    ReceiveThread(Client client, Socket socket) {
        this.client = client;
        this.socket = socket;
    }

    public void run() {
        try {
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        reader = new BufferedReader(new InputStreamReader(inputStream));

        while (true) {
            try {
                line = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (line != null) {
                handleIncomingMessage(line);
            }

        }


    }

    public void handleIncomingMessage(String line) {



        client.addToBeHandled(line);
    }

//        switch (incomingMessage[0]) {
//            case "HELO":
//                client.setConnectionEstablished(true);
//                break;
//
//            case "+OK":
//                client.setValidUsername(true);
//                break;
//
//            case "-ERR":
//                client.setConnectionEstablished(false);
//                break;
//
//            case "PING":
//                client.pingReceived();
//                break;
//
//            case "BCST":
//                System.out.println(incomingMessage[1]);
//        }
}
