package javafinal.tankwar;

import javafinal.tankwar.display.LoginBackground;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class UserLogin extends JFrame {

    public int screenWidth;

    public int screenHeight;

    public int windowWidth;
    public int windowHeight;


    private JLabel welcomLabel;

    private JLabel hintLabel;

    private JTextField inputField;

    private JButton loginButton;

    private JPanel bgPanel;

    public String userName;

    public static Client client;


    public UserLogin() {

        screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;

        screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        windowWidth = Battlefield.w_size.width;
        windowHeight = Battlefield.w_size.height;

        setTitle("Battle City: Tank War");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(windowWidth, windowHeight);
        setResizable(false);
        setBounds((screenWidth - windowWidth) / 2,
                (screenHeight - windowHeight) / 2, windowWidth, windowHeight);
        setLayout(new BorderLayout());

        createBgPanel();
        createWelcomeLabel();
        createHintLabel();
        createInputField();
        createLoginButton();
        bgPanel.add(welcomLabel);
        bgPanel.add(hintLabel);
        bgPanel.add(inputField);
        bgPanel.add(loginButton);

        add(bgPanel);
        setVisible(true);
    }

    private void createBgPanel() {
        bgPanel = new LoginBackground();
        bgPanel.setLayout(null);
        bgPanel.setVisible(true);
    }

    private void createWelcomeLabel() {
        int w = 510, h = 180;
        welcomLabel = new JLabel("<html><br>Welcome to the Battle city!</br><br>Enjoy your own Tank War!</br></html>");
        welcomLabel.setFont(new Font("Arial", Font.BOLD, 40));
        welcomLabel.setBounds((windowWidth - w) / 2, 70, w, h);
        //panel.setVisible(true);
    }

    private void createHintLabel() {
        int w = 210, h = 35;
        hintLabel = new JLabel("Input your own name here: ");
        hintLabel.setForeground(new Color(250, 140, 40));
        hintLabel.setFont(new Font("Arial", Font.PLAIN, 17));
        hintLabel.setBounds((windowWidth - w) / 2, 300, w, h);
    }

    private void createInputField() {
        inputField = new JTextField();
        inputField.setEditable(true);
        int w = 250, h = 35;
        //inputField.setLocation((windowWidth - w) / 2, 250);
        inputField.setBounds((windowWidth - w) / 2, 350, w, h);

    }

    private void createLoginButton() {
        int w = 90, h = 35;
        loginButton = new JButton("Login");
        loginButton.setSize(w, h);
        loginButton.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //System.out.println("mouse click!");
                userName = inputField.getText();
                if (userName == null || userName.isBlank()) {
                    hintLabel.setText("Sorry! You gotta have a valid name!");
                    hintLabel.setForeground(Color.RED);
                    hintLabel.setBounds((windowWidth - 270) / 2, 300, 270, h);
                } else if (userName.contains(",")){
                    hintLabel.setText("Sorry! Your name cannot contain comma!");
                    hintLabel.setForeground(Color.RED);
                    hintLabel.setBounds((windowWidth - 320) / 2, 300, 320, h);
                } else if (userName.contains("=")){
                    hintLabel.setText("Sorry! Your name cannot contain equal sign!");
                    hintLabel.setForeground(Color.RED);
                    hintLabel.setBounds((windowWidth - 350) / 2, 300, 350, h);
                } else {
                    //send name to the server;
                    client.sendName(userName);
                    
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

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
        });
        loginButton.setBounds((windowWidth - w) / 2, 400, loginButton.getWidth(), loginButton.getHeight());
    }


    public static void main(String[] args) {
        UserLogin newUser = new UserLogin();
        client = new Client(newUser);
        new Thread(client).start();
    }
}
