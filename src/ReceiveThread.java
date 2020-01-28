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


    public ReceiveThread(Client client, Socket socket) {
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

        while (!client.pressedQ) {
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
        System.out.println("IN \t << " + line);
        String[] incomingMessage = line.split(" ", 2);

        switch (incomingMessage[0]) {
            case "HELO":
                receivedMessage("HELO");
                break;

            case "+OK":
                handleOkMessages(incomingMessage[1]);
                break;

            case "-ERR":
                System.err.println("\n" + incomingMessage[1]);
                //todo handle error msg
                break;

            case "PING":
                client.pingReceived();
                break;

            case "BCST":
                System.out.println(incomingMessage[1]);
        }
    }

    public void handleOkMessages(String line) {
        if (line.startsWith("HELO")) {
            client.setValidUsername(true);
        }
    }

    public String receivedMessage(String type){
        return type;
    }
}
