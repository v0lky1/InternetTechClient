import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Scanner scanner = new Scanner(System.in);
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 6969;

    private ReceiveThread receiveThread;
    private SendThread sendThread;
    private boolean validUsername;
    private boolean waitForServer;
    protected boolean pressedQ;

    public static void main(String[] args) {
        try {
            new Client().run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void run() throws IOException {
        pressedQ = false;
        Socket socket;

        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);

        waitForServer = true;
        validUsername = false;

        receiveThread = new ReceiveThread(this, socket);
        receiveThread.start();

        sendThread = new SendThread(socket);
        sendThread.start();


        while (!validUsername) {
            if (!waitForServer) {
                String username = setUsername();
                waitForServer = true;
                sendThread.sendMessage("HELO " + username);
            }
        }

        System.out.println("Username OK, go send your messages!");
        while (!pressedQ) {

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
                        pressedQ = true;
                        break;
                }
            } else {
                sendThread.sendMessage("BCST " + message);
            }
        }
    }

    private String setUsername() {
        System.out.print("Enter your preferred username: ");

        return scanner.nextLine();
    }

    public void pingReceived() {
        sendThread.sendMessage("PONG");
    }

    public void setValidUsername(boolean validUsername) {
        this.validUsername = validUsername;
    }

    public boolean hasValidUsername() {
        return validUsername;
    }

    public void setWaitForServer(boolean waitForServer) {
        this.waitForServer = waitForServer;
    }
}
