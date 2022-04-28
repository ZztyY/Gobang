package Server;

import bean.DataPackage;

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
    private HashMap<Socket, ObjectOutputStream> clientMaps = new HashMap<>();
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
            ObjectInputStream inputFromClient = new ObjectInputStream(
                    socket.getInputStream());
            ObjectOutputStream outputToClient = new ObjectOutputStream(
                    socket.getOutputStream());
            this.clientMaps.put(socket, outputToClient);

            // Continuously serve the client
            while (true) {
                if (socket.isClosed()) {
                    socketList.remove(socket);
                    ta.append("Client " + clientNum + " is closed" + "\n");
                    break;
                }
                DataPackage data = (DataPackage) inputFromClient.readObject();
                String instruction = data.getInstruction();
                String message = data.getMessage();
                if (instruction.equals("message")) {
                    for (Socket s: socketList) {
                        ObjectOutputStream oos = this.clientMaps.get(s);
                        if (s == socket) {
                            oos.writeObject(new DataPackage("message", message));
                        } else {
                            oos.writeObject(new DataPackage("message", clientNum + ": " + message));
                        }
                        oos.flush();
                    }
                }
                if (instruction.equals("close")) {
                    outputToClient.writeObject(new DataPackage("close", ""));
                    outputToClient.flush();
                    socket.close();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
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
