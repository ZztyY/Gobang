package Client;

import Client.Constant.Constants;

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

    private Socket socket = null;
    private JTextField textField = null;
    private JTextArea textArea = null;

    public GobangClient() {
        super("Chat Client");
        this.setSize(GobangClient.WIDTH, GobangClient.HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createMenu();

        textField = new JTextField();
        textField.addActionListener(new TextFieldListener());

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(textArea);


        this.add(textField, BorderLayout.SOUTH);
        this.add(scroll, BorderLayout.CENTER);
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

    public void run() {
        try {
            // Create data input and output streams
            DataInputStream inputFromClient = new DataInputStream(
                    socket.getInputStream());

            // Continuously serve the client
            while (true) {
                String data = inputFromClient.readUTF();
                String instruction = data.split("::")[0];
                String body = data.split("::")[1];
                if (instruction.equals("message")) {
                    textArea.append(body + "\n");
                }
                if (instruction.equals("close")) {
                    this.socket.close();
                    textArea.append("disconnected \n");
                    break;
                }

            }
        }
        catch(IOException ex) {
            ex.printStackTrace();
            System.out.println(this.socket.isClosed());
        }
    }

    public static void main(String[] args) {
        GobangClient gobangClient = new GobangClient();
    }

    class OpenConnectionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
            try {
                if (socket == null || socket.isClosed()) {
                    socket = new Socket(Constants.HOST, Constants.PORT);
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
        DataOutputStream toServer;

        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
            try {
                // Create an output stream to send data to the server
                toServer = new DataOutputStream(socket.getOutputStream());
                // Send the radius to the server
                toServer.writeUTF("close::" + " ");
                toServer.flush();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                textArea.append("connection Failure");
            }
        }
    }

    class TextFieldListener implements ActionListener {
        DataOutputStream toServer;

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // Create an output stream to send data to the server
                toServer = new DataOutputStream(socket.getOutputStream());
                // Get the message from the text field
                String message = textField.getText().trim();
                textField.setText("");

                // Send the radius to the server
                toServer.writeUTF("message::" + message);
                toServer.flush();
            }
            catch (IOException ex) {
                System.err.println(ex);
            }
        }
    }
}
