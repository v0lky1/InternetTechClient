import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class SendThread extends Thread {

    private Socket socket;
    private OutputStream outputStream;
    private PrintWriter writer;

    SendThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            outputStream = socket.getOutputStream();
            writer = new PrintWriter(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        // if (!message.equals("PONG")) System.err.println("\tOUT\t >> " + message);

        String command = "";
        String misc = "";

        if (!message.startsWith("PONG")) {
            String[] outgoing = message.split(" ", 2);
            if (outgoing.length != 2 || outgoing[0].isEmpty()){
              System.err.println("Something went wrong");
              return;
            } else {
                command = outgoing[0];
                misc = outgoing[1];
            }
        }
        String[] payload;

        switch (command) {
            case "KICK":
                //0 = groupname, 1 = username
                payload = misc.split(" ", 2);
                System.out.println("Attempting to kick user [" + payload[1] + "] from group: <" + payload[0].toUpperCase() + ">");
                break;

            case "LEAVE":
                System.out.println("Leaving group <" + misc.toUpperCase() + ">");
                break;

            case "RQST":
                if (misc.startsWith("users")) {
                    System.out.println("Requesting all online users from server...");
                } else if (misc.startsWith("groups")) {
                    System.out.println("Requesting all existing groups from server...");
                }
                break;
            case "MAKE":
                System.out.println("Attempting to make group with name <" + misc.toUpperCase() + ">");
                break;

            case "JOIN":
                System.out.println("Attempting to join group with name <" + misc.toUpperCase() + ">");
                break;

            case "SENDFILE":
                break;

        }
        writer.println(message);
        writer.flush();
    }
}
