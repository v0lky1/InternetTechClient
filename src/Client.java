import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.Stack;

public class Client {
    private Scanner scanner = new Scanner(System.in);
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 6969;

    private ReceiveThread receiveThread;
    private SendThread sendThread;
    private boolean validUsername = false;
    private Socket socket;
    private String currentState;
    private String[] incomingMessage;


    private Stack<String> toBeHandled = new Stack<>();

    public static void main(String[] args) {
        try {
            new Client().run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void run() throws IOException {

        setup();

        while (!validUsername) {
            if (!toBeHandled.empty()) {
                incomingMessage = toBeHandled.pop().split(" ", 2);
                currentState = incomingMessage[0];

                //testing stuff
                System.out.println(currentState);
                //-----------------------

                //Switch to handle the username request
                //If we manage to get out of here we have a proper username and can start chatting
                switch (currentState) {
                    case "HELO":
                        setUsername();
                        break;

                    case "+OK":
                        if (incomingMessage[1].startsWith("HELO")) {
                            setValidUsername(true);
                        }
                        break;

                    case "-ERR":
                        if (incomingMessage[1].startsWith("user already logged in")) {
                            System.out.println("That user is already logged in! Try a different account!");
                            setUsername();
                        } else if (incomingMessage[1].startsWith("username has an invalid format")) {
                            System.out.println("That username is invalid! only characters, numbers and underscores are allowed!");
                            setUsername();
                        }
                        break;
                }

            }
        }

        while (true) {
            String message = scanner.nextLine();
            sendThread.sendMessage("BCST " + message);

            if (!toBeHandled.empty()) {
                incomingMessage = toBeHandled.pop().split(" ", 2);
                currentState = incomingMessage[0];

                //TESTING STUFF
                System.out.println(currentState);
                //------------------------------

                switch (currentState) {
                    case "+OK":
                        if (incomingMessage[1].startsWith("BCST")){
                            System.out.println("HERE");
                        }
                    case "BCST":
                        System.out.println(incomingMessage[1]);

                }
                break;
            }
        }
    }

    private void setUsername() {
        System.out.println("Enter your preferred username: ");
        String username = scanner.nextLine();
        sendThread.sendMessage("HELO " + username);
    }

    public void pingReceived() {
        sendThread.sendMessage("PONG");
    }

    private void chatAway() throws IOException {
        while (true) {
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
                        socket.close();
                        break;
                }
            } else {
                sendThread.sendMessage("BCST " + message);
            }
        }
    }

    private void setup() throws IOException {

        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);

        receiveThread = new ReceiveThread(this, socket);
        receiveThread.start();

        sendThread = new SendThread(socket);
        sendThread.start();
    }

    public void setValidUsername(boolean validUsername) {
        this.validUsername = validUsername;
    }

    void addToBeHandled(String message) {
        toBeHandled.push(message);
    }
}
