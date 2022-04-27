package Server;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class GobangServer extends JFrame {
    private ServerSocket serverSocket;
    private final int port = 8888;
    private HashMap<ObjectInputStream, ObjectOutputStream> clientMaps;
    private HashMap<Integer, ArrayList<ObjectInputStream>> roomMap;
    private int roomNums = 0;

    private static int WIDTH = 400;
    private static int HEIGHT = 300;

    // Text area for displaying contents
    private JTextArea ta;

    private int clientNum = 0;
    private ArrayList<Socket> socketList = new ArrayList<>();

    public GobangServer() {
        super("GoBang Server");
        this.setSize(GobangServer.WIDTH, GobangServer.HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        createMenu();

        ta = new JTextArea();
        ta.setEditable(false);
        JScrollPane scroll = new JScrollPane(ta);
        this.add(scroll);

        this.setVisible(true);
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener((e) -> System.exit(0));
        menu.add(exitItem);
        menuBar.add(menu);
        this.setJMenuBar(menuBar);
    }


    public void start() throws IOException {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                this.ta.append("Chat server started at " + new Date() + "\n");
                while (true) {
                    // Listen for a new connection request
                    Socket socket = serverSocket.accept();

                    // Increment clientNo
                    this.clientNum++;
                    this.socketList.add(socket);

                    ta.append("Starting thread for client " + this.clientNum +
                            " at " + new Date() + '\n');

                    // Find the client's host name, and IP address
                    InetAddress inetAddress = socket.getInetAddress();
                    ta.append("Client " + this.clientNum + "'s host name is "
                            + inetAddress.getHostName() + "\n");
                    ta.append("Client " + this.clientNum + "'s IP Address is "
                            + inetAddress.getHostAddress() + "\n");

                    // todo
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            request(socket, clientNum);
                        }
                    }).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void request(Socket socket, int clientNum) {
        try {
            // Create data input and output streams
            DataInputStream inputFromClient = new DataInputStream(
                    socket.getInputStream());

            // Continuously serve the client
            while (true) {
                if (socket.isClosed()) {
                    socketList.remove(socket);
                    ta.append("Client " + clientNum + " is closed" + "\n");
                    break;
                }
                String data = inputFromClient.readUTF();
                String instruction = data.split("::")[0];
                String body = data.split("::")[1];
                if (instruction.equals("message")) {
                    for (Socket s: socketList) {
                        DataOutputStream outputToClient = new DataOutputStream(
                                s.getOutputStream());
                        if (s == socket) {
                            outputToClient.writeUTF("message::" + body);
                        } else {
                            outputToClient.writeUTF("message::" + clientNum + ": " + body);
                        }
                        outputToClient.flush();
                    }
                }
                if (instruction.equals("close")) {
                    DataOutputStream outputToClient = new DataOutputStream(
                            socket.getOutputStream());
                    outputToClient.writeUTF("close:: ");
                    outputToClient.flush();
                    socket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        GobangServer server = new GobangServer();
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
