import java.util.Scanner;

public class ClientInputThread extends Thread {
    private Scanner scanner;
    private Client client;
    private SendThread sendThread;
    private boolean needsUsername;
    private boolean validUsername;
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
        this.validUsername = false;
        this.scanner = new Scanner(System.in);
        client.setClientThreadReady(true);
    }

    public void run() {
        //if nothing stopped our client and username is not valid we need a username.
        while (!validUsername && !client.isStopped()) {
            //??? @Graggor explain this one plz
            if (needsUsername) {
                needsUsername = false;
                setUsername();
            }
        }

        //if nothing stopped our client and our username is valid we can start entering commands
        while (validUsername && !client.isStopped()) {

            //username is valid so set it in client
            client.setUsername(username);

            String message = scanner.nextLine();
            boolean bcst = true;

            //going through all the chat commands to figure out whether its a bcst message or not
            //breaks because otherwise it'll send the message a kazillion times
            //refer to Client.CHAT_COMMANDS for the options
            for (String command : Client.CHAT_COMMANDS) {
                if (message.startsWith(command)) {
                    bcst = false;
                    sendThread.sendMessage(message);
                    if (message.startsWith("QUIT")){
                        client.disconnect();
                    }
                    break;
                }
            }
            //if the command wasnt in Client.CHAT_COMMANDS it defaults to a BCST message.
            if (bcst) {
                sendThread.sendMessage("BCST " + message);
            }
        }
    }

    public void setUsername() {
        System.out.println("Enter your preferred username: ");
        username = scanner.nextLine();
        sendThread.sendMessage("HELO " + username);
    }

    public void setValidUsername(boolean validUsername) {
        this.validUsername = validUsername;
    }

    public void setNeedsUsername(boolean needsUsername) {
        this.needsUsername = needsUsername;
    }
}
