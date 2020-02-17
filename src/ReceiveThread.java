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

        System.out.println("\tIN\t << " + line);

        String[] incomingLine = line.split(" ", 2);



        String state = incomingLine[0];
        String misc = "";
        if (!state.equals("PING")) {
            misc = incomingLine[1];
        }

        switch (state) {
            case "HELO":
                client.setUsername();
                break;

            case "+OK":
                if (misc.startsWith("HELO")) {
                    client.setValidUsername(true);
                    System.out.println("USN OK");
                }
                break;

            case "-ERR":
                //error user is already logged in and error username has invalid format both start with 'user'
                //and have to do the same logic when triggered
                if (misc.startsWith("user")) {
                    System.err.println("Error: " + misc);
                    client.setUsername();
                }
                break;

            case "BCST":
                String[] content = misc.split(" ", 2);
                String username = content[0];
                String message = content[1];

                if (!username.equals(client.getUsername())) {
                    System.out.println(username + " " + message);
                }
                break;

            case "PING":
                client.pingReceived();
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
}
