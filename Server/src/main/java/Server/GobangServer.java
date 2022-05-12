package Server;

import bean.ChessData;
import bean.DataPackage;
import bean.Room;
import bean.User;
import models.DBUtil;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public class GobangServer extends JFrame {
    private ServerSocket serverSocket;
    private final int port = 8888;
    private final Random r = new Random();
    private final HashMap<Socket, ObjectOutputStream> clientMaps = new HashMap<>();
    private final HashMap<Integer, Room> roomMap = new HashMap<>();
    private final ArrayList<Integer> emptyRoom = new ArrayList<>();

    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;

    // Text area for displaying contents
    private final JTextArea ta;

    private int clientNum = 0;
    private final ArrayList<Socket> socketList = new ArrayList<>();

    public GobangServer() {
        super("Gobang Server");
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
                this.ta.append("Gobang server started at " + new Date() + "\n");
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
                    new Thread(() -> request(socket, clientNum)).start();
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
                if (instruction.equals("join")) {
                    int roomNumber = Integer.parseInt(message);
                    Room room = roomMap.get(roomNumber);
                    System.out.println(room);
                    if (room == null) {
                        room = new Room(roomNumber);
                        roomMap.put(roomNumber, room);
                        room.setUser1(socket);
                        emptyRoom.add(roomNumber);
                        outputToClient.writeObject(new DataPackage("joinSuccess", Integer.toString(roomNumber)));
                        outputToClient.flush();
                    } else {
                        if (room.getUser2() != null) {
                            outputToClient.writeObject(new DataPackage("joinFail", "Room is full !"));
                            outputToClient.flush();
                        } else {
                            room.setUser2(socket);
                            outputToClient.writeObject(new DataPackage("startGame", ""));
                            outputToClient.flush();


                            ObjectOutputStream oos = this.clientMaps.get(room.getUser1());
                            oos.writeObject(new DataPackage("startGame", Integer.toString(roomNumber)));
                            oos.flush();
                        }
                    }
                }
                if (instruction.equals("randomMatch")) {
                    if (emptyRoom.size() != 0) {
                        Room room = roomMap.get(emptyRoom.get(0));
                        emptyRoom.remove(0);
                        room.setUser2(socket);
                        outputToClient.writeObject(new DataPackage("startGame", Integer.toString(room.getRoomNumber())));
                        outputToClient.flush();
                        ObjectOutputStream oos = this.clientMaps.get(room.getUser1());
                        oos.writeObject(new DataPackage("startGame", Integer.toString(room.getRoomNumber())));
                        oos.flush();
                    } else {
                        int roomNumber = r.nextInt(100) +100;
                        while (roomMap.get(roomNumber) != null) {
                            roomNumber = r.nextInt(100) +100;
                        }
                        Room room = new Room(roomNumber);
                        roomMap.put(roomNumber, room);
                        room.setUser1(socket);
                        emptyRoom.add(roomNumber);
                        outputToClient.writeObject(new DataPackage("joinSuccess", Integer.toString(roomNumber)));
                        outputToClient.flush();
                    }
                }
                if (instruction.equals("down")) {
                    ChessData chessData = (ChessData) inputFromClient.readObject();
                    Room room = roomMap.get(chessData.getRoomNumber());
                    ObjectOutputStream oos;
                    if (chessData.isOffense()) {
                        // get rival, offense is player1 then the rival is player2
                        oos = clientMaps.get(room.getUser2());
                    } else {
                        oos = clientMaps.get(room.getUser1());
                    }
                    oos.writeObject(new DataPackage("down", ""));
                    oos.flush();
                    oos.writeObject(chessData);
                    oos.flush();
                }
                if (instruction.equals("rank")) {
                    Connection conn = DBUtil.getConn();
                    String rank = "";
                    String sql = "select * from user order by wins desc";
                    PreparedStatement preSt = conn.prepareStatement(sql);
                    ResultSet res = preSt.executeQuery();
                    while (res.next()) {
                        rank += "Username: " + res.getString("username") +
                                " Wins: " + res.getInt("wins") + "\n";
                    }
                    outputToClient.writeObject(new DataPackage("rank", rank));
                    outputToClient.flush();
                }
                if (instruction.equals("signup")) {
                    User user = (User) inputFromClient.readObject();
                    try {
                        if (models.User.findUserByName(user.getUsername()) == null) {
                            models.User.createUser(user.getUsername(), user.getPassword());
                            outputToClient.writeObject(new DataPackage("signup", "Signup success !"));
                            outputToClient.flush();
                        } else {
                            outputToClient.writeObject(new DataPackage("signup", "Signup fail, try another username !"));
                            outputToClient.flush();
                        }
                    } catch (SQLException e) {
                        outputToClient.writeObject(new DataPackage("signup", "Signup fail, try another username !"));
                        outputToClient.flush();
                    }

                }
                if (instruction.equals("login")) {
                    User user = (User) inputFromClient.readObject();
                    try {
                        models.User u = models.User.findUser(user.getUsername(), user.getPassword());
                        outputToClient.writeObject(new DataPackage("login", "success"));
                        outputToClient.flush();
                        user.setId(u.getId());
                        user.setWins(u.getWins());
                        outputToClient.writeObject(user);
                        outputToClient.flush();
                    } catch (SQLException e) {
                        outputToClient.writeObject(new DataPackage("login", "fail"));
                        outputToClient.flush();
                    }
                }
            }
        } catch (IOException | ClassNotFoundException | SQLException e) {
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
