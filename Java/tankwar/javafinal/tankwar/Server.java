package javafinal.tankwar;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {

    private ServerSocket serverSocket;
    private int port = 9898;

    private PriorityQueue<SingleRecord> scoreRecord;

    private PriorityQueue<SingleRecord> temp;

    //private HashMap<String, PriorityQueue<SingleRecord>> scoreRecord = new HashMap<>();;
    //the key would be the client name
    private HashMap<Socket, String> clientConnection;
    //the key would be the client socket


    public void Server() {
    }


    public void startServer() {
        this.scoreRecord = new PriorityQueue<SingleRecord>((a, b) -> {
            if (a.score == b.score) {
                return b.time.compareTo(a.time);
            }
            return b.score - a.score;
        });
        this.temp = new PriorityQueue<SingleRecord>((a, b) -> {
            if (a.score == b.score) {
                return b.time.compareTo(a.time);
            }
            return b.score - a.score;
        });
        this.clientConnection = new HashMap<>();
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                clientConnection.put(socket, null);
                ServiceTunnel serviceTunnel = new ServiceTunnel(socket);
                new Thread(serviceTunnel).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
    }

    class ServiceTunnel implements Runnable {

        private Socket socket;
        private String clientName;

        private int clientScore;

        private DataInputStream in;
        private DataOutputStream out;

        private Date date;

        public ServiceTunnel(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                while (true) {
                    try{
                        if (!clientConnection.containsKey(socket)) {
                            break;
                        }
                        if (clientConnection.get(socket) == null) {
                            //not obtain the client name yet
                            clientName = in.readUTF();
                            clientConnection.put(socket, clientName);
                            //System.out.println("server receives the client name: " + clientName);
                        } else {
                            clientScore = in.readInt();
                            if (clientScore == -1) {

                                //the game ranking list is requested
                                List<SingleRecord> list = new ArrayList<>();
                                for (int i = 0; i < 10; i++) {
                                    if (scoreRecord.isEmpty()) {
                                        break;
                                    }
                                    SingleRecord record = scoreRecord.poll();
                                    list.add(record);
                                    temp.offer(record);
                                }
                                scoreRecord.addAll(temp);
                                temp = new PriorityQueue<SingleRecord>((a, b) -> {
                                    if (a.score == b.score) {
                                        return b.time.compareTo(a.time);
                                    }
                                    return b.score - a.score;
                                });
                                String allScores = "";
                                for (SingleRecord record: list) {
                                    String r = record.name + " == " + record.score + " == " + record.time;
                                    if (allScores.equals("")) {
                                        allScores = r;
                                    } else {
                                        allScores = allScores + "," + r;
                                    }
                                }
                                out.writeUTF(allScores);

                            } else {
                                date = new Date();
                                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                String time = df.format(date);

                                SingleRecord singleRecord = new SingleRecord(clientName, clientScore, time);
                                System.out.println("client " + clientName + " got " + clientScore + " at " + time);
                                scoreRecord.add(singleRecord);
                            }
                        }

                    } catch (EOFException e) {
                        System.out.println("Client has been disconnected.");
                        clientConnection.remove(socket);
                        break;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    class SingleRecord {
        private String name;
        private int score;
        private String time;

        public SingleRecord(String name, int score, String time) {
            this.name = name;
            this.score = score;
            this.time = time;
        }
    }
}

