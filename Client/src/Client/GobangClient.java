package Client;

import Constants.Constants;
import bean.ChessData;
import bean.DataPackage;
import bean.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
    private ChessboardPanel chessboardPage;
    private int roomNum;
    private boolean flag = false;
    private boolean offense = false;

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
        randMat.addActionListener(new RandMatButtonListener());
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

    private void createChessboardPage() {
        this.setSize(1000, 1000);
        chessboardPage = new ChessboardPanel();

        this.add(chessboardPage);
    }

    private void deleteChessboardPage() {
        this.remove(chessboardPage);
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
                if (instruction.equals("joinSuccess")) {
                    offense = true;
                    roomNum = Integer.parseInt(message);
                    System.out.println(instruction+ " " +roomNum);
                    JOptionPane.showMessageDialog(joinRoomPage, "Join room success, waiting for game begin !");
                }
                if (instruction.equals("startGame")) {
                    if (offense) {
                        flag = true;
                    }
                    roomNum = Integer.parseInt(message);
                    System.out.println(instruction+ " " +roomNum);
                    if (joinRoomPage != null) {
                        deleteJoinRoomPage();
                    }
                    if (mainPage != null) {
                        deleteMainPage();
                    }
                    createChessboardPage();
                    repaint();
                    validate();
                }
                if (instruction.equals("joinFail")) {
                    JOptionPane.showMessageDialog(joinRoomPage, message, message,JOptionPane.WARNING_MESSAGE);
                }
                if (instruction.equals("down")) {
                    ChessData chessData = (ChessData) inputFromClient.readObject();
                    int[] chess = chessData.getChess();
                    chessboardPage.makeChess(chess[0], chess[1]);
                    flag = true;
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

    class RandMatButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                toServer.writeObject(new DataPackage("randomMatch", ""));
                toServer.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
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
                ex.printStackTrace();
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
                ex.printStackTrace();
            }
        }
    }

    class ChessboardPanel extends JPanel implements MouseListener {
        public int x;
        public int y;
        public int chessX;
        public int chessY;
        public Game game = new Game();
        public JLabel roomLabel = new JLabel("room:" + roomNum);

        public ChessboardPanel() {
            super();
            setLayout(null);
            addMouseListener(this);
            roomLabel.setBounds(0, 150, 100, 50);
            add(roomLabel);
        }

        public void paint(Graphics g) {
            super.paint(g);
            game.Paint(g);
        }

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            x = e.getX();
            y = e.getY();
            if (x>=100 && x<=900  && y>=100 && y<=900) {
                chessX = (x - 100) / 50;
                chessY = (y - 100) / 50;
                if (flag){
                    makeChess(chessX,chessY);
                    int[] chess = new int[]{chessX,chessY};
                    try {
                        toServer.writeObject(new DataPackage("down", ""));
                        toServer.flush();
                        toServer.writeObject(new ChessData(roomNum, chess, offense));
                        toServer.flush();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    flag = false;
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        public void makeChess(int x, int y) {
            int[] B = game.Judge(x, y);
            repaint();
            int b = game.P();
            if (b == 1)
            {
                JOptionPane.showMessageDialog(this,"game over, black wins");
                game.Clean();
                game.Rush();
                flag = false;
            }
            else if (b == 2)
            {
                JOptionPane.showMessageDialog(this,"game over, white wins");
                game.Clean();
                game.Rush();
                flag = false;
            }
            Boolean Pe = game.Peace();
            if (Pe)
            {
                JOptionPane.showMessageDialog(this,"game over, draw");
                game.Clean();
                game.Rush();
                flag = false;
            }
            repaint();
        }
    }
}
