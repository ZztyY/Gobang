package Client;

import Constants.Constants;
import bean.DataPackage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class GobangClient extends JFrame implements Runnable {

    /**
     *
     */
    private static final long serialVersionUID = 3506667960685270607L;
    private static int WIDTH = 400;
    private static int HEIGHT = 300;

    private JPanel mainPage;

    // join room page variables
    private JPanel joinRoomPage;
    private JTextField roomNumber;

    // chessboard page variables
    private JPanel chessboardPage;
    private int roomNum;

    private Socket socket = null;
    private ObjectOutputStream toServer;
    private JTextField textField;
    private JTextArea textArea;

    public GobangClient() {
        super("Gobang Client");
        this.setSize(GobangClient.WIDTH, GobangClient.HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createMenu();

        //createChatRoom();
        createMainPage();
        //deleteMainPage();
        //createJoinRoomPage();
        this.setVisible(true);

    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener((e) -> System.exit(0));
        JMenuItem connectItem = new JMenuItem("Connect");
        connectItem.addActionListener(new OpenConnectionListener());
        JMenuItem disconnectItem = new JMenuItem("disconnect");
        disconnectItem.addActionListener(new CloseConnectionListener());
        menu.add(connectItem);
        menu.add(disconnectItem);
        menu.add(exitItem);
        menuBar.add(menu);
        this.setJMenuBar(menuBar);
    }

    private void createChatRoom() {
        textField = new JTextField();
        textField.addActionListener(new TextFieldListener());

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(textArea);


        this.add(textField, BorderLayout.SOUTH);
        this.add(scroll, BorderLayout.CENTER);
    }

    private void createMainPage() {
        mainPage = new JPanel();
        mainPage.setLayout(null);

        JButton randMat = new JButton("Random Match");
        randMat.setBounds(90, 50, 200, 50);
        randMat.addActionListener(null);  // todo
        JButton joinRoom = new JButton("Join Room");
        joinRoom.setBounds(90, 130, 200, 50);
        joinRoom.addActionListener(new JoinRoomButtonListener());

        mainPage.add(randMat);
        mainPage.add(joinRoom);
        this.add(mainPage);
    }

    private void deleteMainPage() {
        this.remove(mainPage);
    }

    private void createJoinRoomPage() {
        joinRoomPage = new JPanel();
        joinRoomPage.setLayout(null);

        JLabel rn = new JLabel("Room Number: ");
        rn.setBounds(20, 30, 150, 30);

        roomNumber = new JTextField();
        roomNumber.setBounds(40, 70, 300, 30);

        JButton clear = new JButton("clear");
        clear.addActionListener(new ClearButtonListener());
        JButton join = new JButton("join");
        join.addActionListener(new JoinButtonListener());
        clear.setBounds(220, 130, 70, 30);
        join.setBounds(300, 130, 60, 30);

        JButton back = new JButton("back");
        back.addActionListener(new BackButtonListener());
        back.setBounds(260, 180, 70, 30);

        joinRoomPage.add(rn);
        joinRoomPage.add(roomNumber);
        joinRoomPage.add(clear);
        joinRoomPage.add(join);
        joinRoomPage.add(back);
        this.add(joinRoomPage);
    }

    private void deleteJoinRoomPage() {
        this.remove(joinRoomPage);
    }

    // todo chessboard
    private void createChessboardPage() {
        chessboardPage = new JPanel();

        this.add(chessboardPage);
    }

    private void deleteChessboardPage() {
        this.remove(joinRoomPage);
    }

    public void run() {
        try {
            // Create data input and output streams
            ObjectInputStream inputFromClient = new ObjectInputStream(
                    socket.getInputStream());

            // Continuously serve the client
            while (true) {
                DataPackage data = (DataPackage) inputFromClient.readObject();
                String instruction = data.getInstruction();
                String message = data.getMessage();
                if (instruction.equals("message")) {
                    textArea.append(message + "\n");
                }
                if (instruction.equals("close")) {
                    this.socket.close();
                    textArea.append("disconnected \n");
                    break;
                }
                if (instruction.equals("join")) {
                    deleteJoinRoomPage();
                    // todo create chessboard
                }

            }
        } catch(IOException ex) {
            ex.printStackTrace();
            System.out.println(this.socket.isClosed());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new GobangClient();
    }

    class JoinRoomButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            deleteMainPage();
            createJoinRoomPage();
            repaint();
            validate();
        }
    }

    class BackButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            deleteJoinRoomPage();
            createMainPage();
            repaint();
            validate();
        }
    }

    class ClearButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            roomNumber.setText("");
        }
    }

    class JoinButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // Get the message from the text field
                String message = roomNumber.getText().trim();
                roomNumber.setText("");

                // Send the radius to the server
                toServer.writeObject(new DataPackage("join", message));
                toServer.flush();
            }
            catch (IOException ex) {
                System.err.println(ex);
            }
        }
    }

    class OpenConnectionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (socket == null || socket.isClosed()) {
                    socket = new Socket(Constants.HOST, Constants.PORT);
                    toServer = new ObjectOutputStream(socket.getOutputStream());
                    Thread t = new Thread(GobangClient.this);
                    t.start();
                    textArea.append("connected \n");
                }
            } catch (IOException e1) {
                e1.printStackTrace();
                textArea.append("connection Failure");
            }
        }
    }

    class CloseConnectionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // Send the close info to the server
                toServer.writeObject(new DataPackage("close", ""));
                toServer.flush();
            } catch (IOException e1) {
                e1.printStackTrace();
                textArea.append("connection Failure");
            }
        }
    }

    class TextFieldListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // Get the message from the text field
                String message = textField.getText().trim();
                textField.setText("");

                // Send the radius to the server
                toServer.writeObject(new DataPackage("message", message));
                toServer.flush();
            }
            catch (IOException ex) {
                System.err.println(ex);
            }
        }
    }
}
