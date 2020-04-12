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
        if (!message.equals("PONG")) System.err.println("\tOUT\t >> " + message);
        writer.println(message);
        writer.flush();
    }
}
