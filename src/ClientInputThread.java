import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientInputThread extends Thread {
    private Scanner scanner;
    private Client client;
    private SendThread sendThread;
    private boolean needsUsername;
    private final AtomicBoolean validUsername = new AtomicBoolean(false);
    private String username;

    /**
     * the message sender thread, solely responsible for talking to the sendThread class to get messages across.
     * We stumbled upon some issues regarding race-conditions and certain message-processing functionality that
     * made the program synchronous.
     * @param client makes sure we can tell the client what state he's in
     * @param sendThread is here because it's the thread responsible for getting our messages to the server
     */
    public ClientInputThread(Client client, SendThread sendThread) {
        this.client = client;
        this.sendThread = sendThread;
        this.scanner = new Scanner(System.in);
        client.setClientThreadReady(true);
    }

    public void run() {
        //if nothing stopped our client and username is not valid we need a username.
        while (!validUsername.get() && !client.isStopped()) {
            // If we do not yet have a username the client should receive HELO from the server.
            // Once they receive HELO we know the server is ready for our username and set "needsUsername" to true.
            // We instantly set needsUsername to false so we don't get into this loop.
            // After that we ask the user for a username, send it to the server and wait for a response.
            // If the server says our username is taken we can set "needsUsername" to true again to request a new one.
            // If the server approves the username we go out of the surrounding while loop and continue the program.
            if (needsUsername) {
                needsUsername = false;
                setUsername();
            }
        }

        //username is valid so set it in client
        client.setUsername(username);

        //if nothing stopped our client and our username is valid we can start entering commands
        while (!client.isStopped()) {
            String message = scanner.nextLine();

            // Commands should have a leading '/' to differentiate them from all-caps messages intended for broadcast.
            // Starts with '/'? We have a command.
            if (message.startsWith("/")) {
                boolean validCommand = false;
                // Remove the leading '/' so we can process the rest of the message.
                message = message.substring(1);

                // The below code splits the message into a command and the rest of the message, capitalizes the command segment.
                // Afterwards it puts it back together for user convenience.
                String[] splitMessage = message.split(" ", 2);
                String userCommand = splitMessage[0].toUpperCase();

                message = splitMessage.length == 1 ? userCommand : userCommand + " " + splitMessage[1];

                for (String command : Client.CHAT_COMMANDS){
                    if (command.equals(userCommand)){
                        validCommand = true;
                        sendThread.sendMessage(message);
                        if (command.equals("QUIT")){
                            client.disconnect();
                        }
                    }
                }
                if (!validCommand) System.out.println("UNKNOWN COMMAND: " + userCommand);
            } else {
                sendThread.sendMessage("BCST " +  message);
            }
        }
    }

    public void setUsername() {
        System.out.println("Enter your preferred username: ");
        username = scanner.nextLine();
        sendThread.sendMessage("HELO " + username);
    }

    public void setValidUsername(boolean validUsername) {
        this.validUsername.set(validUsername);
    }

    public void setNeedsUsername(boolean needsUsername) {
        this.needsUsername = needsUsername;
    }
}
