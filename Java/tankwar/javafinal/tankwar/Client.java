package javafinal.tankwar;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;

public class Client implements Runnable{

    private int port = 9898;

    private String host = "localhost";

    private DataInputStream in;
    private DataOutputStream out;

    private Socket socket;

    private Date date;

    public UserLogin userLogin;

    public boolean gameStart = false;
    public boolean gameOver = false;

    public int score;

    public String allScores = "";

    public Client(UserLogin userLogin) {
        this.userLogin = userLogin;
        try {
            socket = new Socket(host, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            //fail to connect;
            e.printStackTrace();
        }
    }

    public void sendName(String name) {
        try {
            out.writeUTF(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Battlefield battle = new Battlefield(this, userLogin);
        battle.setVisible(true);
        battle.loadResource();
    }

    public void sendScore(int score) {
        try {
            out.writeInt(score);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendRankRequest() {
        try {
            out.writeInt(-1);
            allScores = in.readUTF();
           // System.out.println("client receive: " + allScores);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public void run() {
        while (true) {
            if (gameStart && gameOver) {
                gameStart = false;
                gameOver = false;
                sendScore(score);
            }

        }
    }
}
