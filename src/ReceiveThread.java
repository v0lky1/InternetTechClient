
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;
import java.util.Base64;

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

        while (!client.isStopped()) {
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
        //  if (!line.equals("PING") && !line.contains("+USR") && !line.contains("+EOL"))
        //    System.out.println("\tIN\t << " + line);

        String[] incomingLine = line.split(" ", 2);

        String state = incomingLine[0];
        String misc = "";
        if (!state.equals("PING")) {
            misc = incomingLine[1];
        }

        String[] payload;
        switch (state) {
            case "HELO":
                client.setNeedsUsername(true);
                break;

            case "+OK":
                //follow up commands
                payload = misc.split(" ", 2); //0 = command 1 = de rest
                String command = payload[0];
                switch (command) {
                    case "HELO":
                        client.setValidUsername(true);
                        System.out.println("Username set! You can now start chatting.");
                        break;
                    case "BCST":
                        //1 = message
                        System.out.println("Me: " + payload[1]);
                        break;
                    case "MAKE":
                        //1 = groupname
                        System.out.println("Group succesfully made! Message them by using command /grpmsg " + payload[1] + " <message>");
                        break;
                    case "JOIN":
                        //misc = groupname
                        System.out.println("Group succesfully joined! Message them by using command /grpmsg " + payload[1] + " <message>");
                        break;
                    case "LEAVE":
                        //1 = groupname
                        System.out.println("Leaving group <" + payload[1] + ">");
                        break;
                    case "KICK":
                        //0 = groupname,  1 = kicked person
                        payload = payload[1].split(" ", 2);
                        System.out.println("Kicked user: " + payload[1] + " from group <" + payload[0].toUpperCase() + ">");
                        break;
                    case "GRPMSG":
                        //0 = groupname, 1 is message
                        payload = payload[1].split(" ", 2);
                        System.out.println("<" + payload[0].toUpperCase() + ">" + "Me: " + payload[1]);
                        break;
                    case "DM":
                        //0 = recipient username, 1 = message
                        payload = payload[1].split(" ", 2);
                        System.out.println("to [" + payload[0] + "]: " + payload[1]);
                        break;

                    case "users": //LISTREQUESTS
                        System.out.print("List of online users: ");
                        break;
                    case "groups":
                        System.out.println("List of existing groups: ");
                        break;
                }
                break;

            //merged these cases since they're the same
            case "+USR":
            case "+GRP":
                System.out.print("[" + misc + "] ");
                break;
            case "+EOL":
                //so a new system.in won't start directly next to the list but on a new line
                System.out.println();
                break;

            case "-ERR":
                //error user is already logged in and error username has invalid format both start with 'user'
                //and have to do the same logic when triggered
                if (misc.startsWith("user already logged in") || misc.startsWith("user has an invalid format")) {
                    System.err.println("Error: " + misc);
                    client.setNeedsUsername(true);
                } else {
                    System.err.println("Error: " + misc);
                }
                break;

            case "BCST":
                payload = misc.split(" ", 2);
                //0 = sender, 1 = message
                if (!payload[0].equals(client.getUsername())) {
                    System.out.println(payload[0] + ": " + payload[1]);
                }
                break;

            case "REMOVED":
                System.out.println("You've been removed from group " + "<" + misc.toUpperCase() + ">");
                break;

            case "DM":
                payload = misc.split(" ", 2);
                //0 = sender, 1 = message
                System.out.println("from [" + payload[0] + "]: " + payload[1]);
                break;
            case "GRPMSG":
                payload = misc.split(" ", 3);
                //0 = group, 1 = username, 2 = message
                System.out.println("<" + payload[0].toUpperCase() + ">" + " from [" + payload[1] + "]: " + payload[2]);
                break;

            case "RECEIVEFILE":
                payload = misc.split(" ", 3);
                // 0 = username, 1 = filename, 2 = bytearray in string format
                System.out.println("Received file from " + payload[0] + " with filename: " + payload[1]);
                byte[] file = Base64.getDecoder().decode(payload[2]);
                client.retrieveFile(payload[1], file);
                break;

            case "PING":
                client.pingReceived();
                break;

            case "DSCN":
                System.err.println("Didn't send send-alive in time!");
                client.disconnect();
                break;
        }
    }
}
