import java.util.Scanner;

public class ClientInputThread extends Thread {
    private Scanner scanner;
    private Client client;
    private SendThread sendThread;
    private boolean needsUsername;
    private boolean validUsername;
    private String username;

    public ClientInputThread(Client client, SendThread sendThread) {
        this.client = client;
        this.sendThread = sendThread;
        this.validUsername = false;
        this.scanner = new Scanner(System.in);
        client.setClientThreadReady(true);
    }

    public void run() {
        while (!validUsername && !client.isStopped()){
            if (needsUsername) {
                needsUsername = false;
                setUsername();
            }
        }

        while(validUsername && !client.isStopped()){
            //username is valid so set it in client
            client.setUsername(username);
            String message = scanner.nextLine();

            if (message.startsWith("/")) {
                //removing the / because we know a command has been given
                //and we're in this if
                message = message.substring(1);
                //limit 2 because this would always we force the user
                //to type command " " parameter.
                String[] command = message.split(" ", 2);

                //forcing uppercase so we dont have to deal with
                //lower case examples
                switch (command[0].toUpperCase()) {
                    case "Q":
                        sendThread.sendMessage("QUIT");
                        client.disconnect();
                        break;
                }
            } else {
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
