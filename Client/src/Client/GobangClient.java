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

    private Socket socket = null;
    private ObjectOutputStream toServer;
    private JTextField textField;
    private JTextArea textArea;

    public GobangClient() {
        super("Gobang Client");
        this.setSize(GobangClient.WIDTH, GobangClient.HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createMenu();

        createChatRoom();
        //createMainPage();
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
        randMat.addActionListener(null); // todo
        JButton joinRoom = new JButton("Join Room");
        joinRoom.setBounds(90, 130, 200, 50);
        joinRoom.addActionListener(null); // todo

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
        JButton join = new JButton("join");
        clear.setBounds(220, 130, 70, 30);
        join.setBounds(300, 130, 60, 30);

        joinRoomPage.add(rn);
        joinRoomPage.add(roomNumber);
        joinRoomPage.add(clear);
        joinRoomPage.add(join);
        this.add(joinRoomPage);
    }

    private void deleteJoinRoomPage() {
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

    class OpenConnectionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
            try {
                if (socket == null || socket.isClosed()) {
                    socket = new Socket(Constants.HOST, Constants.PORT);
                    toServer = new ObjectOutputStream(socket.getOutputStream());
                    Thread t = new Thread(GobangClient.this);
                    t.start();
                    textArea.append("connected \n");
                }
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                textArea.append("connection Failure");
            }
        }
    }

    class CloseConnectionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
            try {
                // Send the close info to the server
                toServer.writeObject(new DataPackage("close", ""));
                toServer.flush();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
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
