import java.io.*;
import java.net.Socket;

public class Client {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 6969;
    //leaving BCST out of this one because it's being used in ClientInputThread as default.
    public static final String[] CHAT_COMMANDS = {"DM", "RQST", "MAKE", "JOIN", "GRPMSG", "LEAVE", "KICK", "QUIT", "SENDFILE"};

    private ReceiveThread receiveThread;
    private SendThread sendThread;
    private ClientInputThread clientInputThread;

    private Socket socket;
    private String username;

    private boolean stopped;
    private boolean clientThreadReady;

    public static void main(String[] args) {
        try {
            new Client().run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void run() throws IOException {
        setup();
    }

    private void setup() throws IOException {
        stopped = false;
        clientThreadReady = false;
        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);

        sendThread = new SendThread(socket);
        clientInputThread = new ClientInputThread(this, sendThread);
        receiveThread = new ReceiveThread(this, socket);

        clientInputThread.start();

        while (true) {
            if (clientThreadReady) {
                receiveThread.start();
                break;
            }
        }
        sendThread.start();
    }

    public void setNeedsUsername(boolean needsUsername) {
        clientInputThread.setNeedsUsername(needsUsername);
    }

    public void pingReceived() {
        sendThread.sendMessage("PONG");
    }

    public void setValidUsername(boolean validUsername) {
        clientInputThread.setValidUsername(validUsername);
    }

    public void disconnect() {
        try {
            stopped = true;
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setClientThreadReady(boolean clientThreadReady) {
        this.clientThreadReady = clientThreadReady;
    }

    public boolean isStopped() {
        return stopped;
    }

    public byte[] getFileContents(String filename){
        try {
            File file = new File(filename);
            FileInputStream fileInputStream = new FileInputStream(file);

            System.out.println("Size in bytes: " + fileInputStream.available());

            byte[] totalcontent = new byte[fileInputStream.available()];
            int content;
            int index = 0;
            while ((content = fileInputStream.read()) != -1) {
                totalcontent[index] = (byte)content;
                index++;
            }
            return totalcontent;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void retrieveFile(String filename, byte[] contents){
        try {
            // Set the directory to "received"
            File targetDirectory = new File("received");
            targetDirectory.mkdir();
            File targetFile = new File(targetDirectory, filename);

            FileOutputStream fileOutputStream = new FileOutputStream(targetFile);

            fileOutputStream.write(contents);
            fileOutputStream.flush();
            fileOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
