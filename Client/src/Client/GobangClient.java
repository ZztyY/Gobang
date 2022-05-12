package Client;

import Constants.Constants;
import bean.ChessData;
import bean.DataPackage;
import bean.Game;
import bean.User;

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
    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;

    // track the current page
    private JPanel currentPage;

    private JPanel welcomePage;
    private JPanel rankPage;
    private JPanel loginPage;

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
    private JMenu menu2;
    private User self;

    public GobangClient() {
        super("Gobang Client");
        this.setSize(GobangClient.WIDTH, GobangClient.HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createMenu();

        //createChatRoom();
        //createMainPage();
        createWelcomePage();
        this.setVisible(true);

    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu1 = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener((e) -> System.exit(0));
        JMenuItem connectItem = new JMenuItem("Connect");
        connectItem.addActionListener(new OpenConnectionListener());
        JMenuItem disconnectItem = new JMenuItem("Disconnect");
        disconnectItem.addActionListener(new CloseConnectionListener());
        menu2 = new JMenu("User");
        JMenuItem rankItem = new JMenuItem("Rank");
        rankItem.addActionListener(new GetRankPageListener());
        JMenuItem loginItem = new JMenuItem("login");
        loginItem.addActionListener(new GetLoginPageListener());
        JMenuItem backItem = new JMenuItem("back");
        backItem.addActionListener(new BackButtonListener());
        menu1.add(connectItem);
        menu1.add(disconnectItem);
        menu1.add(exitItem);
        menuBar.add(menu1);
        menu2.add(loginItem);
        menu2.add(rankItem);
        menu2.add(backItem);
        menuBar.add(menu2);
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

    private void createWelcomePage() {
        welcomePage = new JPanel();
        welcomePage.setLayout(null);
        JLabel welcome = new JLabel("Welcome to GoBang!");
        welcome.setBounds(120, 80, 200, 50);

        welcomePage.add(welcome);
        this.add(welcomePage);
        currentPage = welcomePage;
    }

    private void deleteWelcomePage() {
        this.remove(currentPage);
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
        currentPage = mainPage;
    }

    private void deleteMainPage() {
        this.remove(currentPage);
    }

    private void createRankPage(String rank) {
        rankPage = new JPanel();
        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        ta.append(rank);
        JScrollPane  jScrollPane = new JScrollPane(ta);

        rankPage.add(jScrollPane);
        this.add(rankPage);
        currentPage = rankPage;
    }

    private void createLoginPage() {
        loginPage = new JPanel();
        loginPage.setLayout(null);

        JLabel nameLabel = new JLabel("username: ");
        nameLabel.setBounds(20, 30, 100, 30);

        JTextField username = new JTextField();
        username.setBounds(120, 30, 200, 30);

        JLabel passwordLabel = new JLabel("password: ");
        passwordLabel.setBounds(20, 70, 100, 30);

        JTextField password = new JTextField();
        password.setBounds(120, 70, 200, 30);

        JButton signup = new JButton("signup");
        signup.addActionListener(new SignupButtonListener(username, password));
        JButton login = new JButton("login");
        login.addActionListener(new LoginButtonListener(username, password));
        signup.setBounds(190, 130, 90, 30);
        login.setBounds(300, 130, 80, 30);

        loginPage.add(nameLabel);
        loginPage.add(username);
        loginPage.add(passwordLabel);
        loginPage.add(password);
        loginPage.add(signup);
        loginPage.add(login);

        this.add(loginPage);
        currentPage = loginPage;
    }

    private void deleteCurrentPage() {
        this.remove(currentPage);
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
        currentPage = joinRoomPage;
    }

    private void deleteJoinRoomPage() {
        this.remove(currentPage);
    }

    private void createChessboardPage() {
        this.setSize(1000, 1000);
        chessboardPage = new ChessboardPanel();

        this.add(chessboardPage);
        currentPage = chessboardPage;
    }

    private void deleteChessboardPage() {
        this.remove(currentPage);
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
                    if (textArea != null) {
                        textArea.append("disconnected \n");
                    }
                    this.remove(currentPage);
                    createWelcomePage();
                    repaint();
                    validate();
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
                if (instruction.equals("rank")) {
                    this.remove(currentPage);
                    createRankPage(message);
                    repaint();
                    validate();
                }
                if (instruction.equals("signup")) {
                    JOptionPane.showMessageDialog(currentPage, message);
                }
                if (instruction.equals("login")) {
                    if (message.equals("success")) {
                        self = (User) inputFromClient.readObject();
                        menu2.setText(self.getUsername());
                        deleteCurrentPage();
                        createMainPage();
                        repaint();
                        validate();
                    } else {
                        JOptionPane.showMessageDialog(currentPage, "Login fail, please try again !");
                    }
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

    class SignupButtonListener implements ActionListener {
        private final JTextField username;
        private final JTextField password;

        public SignupButtonListener(JTextField name, JTextField pass) {
            username = name;
            password = pass;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String uname = username.getText();
            String pwd = password.getText();
            username.setText("");
            password.setText("");
            User user = new User(uname, pwd);

            try {
                toServer.writeObject(new DataPackage("signup", ""));
                toServer.flush();
                toServer.writeObject(user);
                toServer.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    class LoginButtonListener implements ActionListener {
        private final JTextField username;
        private final JTextField password;

        public LoginButtonListener(JTextField name, JTextField pass) {
            username = name;
            password = pass;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            String uname = username.getText();
            String pwd = password.getText();
            username.setText("");
            password.setText("");
            User user = new User(uname, pwd);

            try {
                toServer.writeObject(new DataPackage("login", ""));
                toServer.flush();
                toServer.writeObject(user);
                toServer.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
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
            // if not connected will not redirect to main page
            if (currentPage != welcomePage) {
                deleteJoinRoomPage();
                createMainPage();
                repaint();
                validate();
            }
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

    class GetRankPageListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (socket != null && !socket.isClosed()) {
                    // Send the get rank info request to the server
                    toServer.writeObject(new DataPackage("rank", ""));
                    toServer.flush();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    class GetLoginPageListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // if not connected will not redirect to main page
            if (currentPage != welcomePage) {
                deleteCurrentPage();
                createLoginPage();
                repaint();
                validate();
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

                    deleteWelcomePage();
                    createMainPage();
                    repaint();
                    validate();
                    if (textArea != null) {
                        textArea.append("connected \n");
                    }
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
                if (socket!=null) {
                    // Send the close info to the server
                    toServer.writeObject(new DataPackage("close", ""));
                    toServer.flush();
                }
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
