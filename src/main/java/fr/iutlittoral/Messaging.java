package fr.iutlittoral;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONObject;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class Messaging extends BorderPane {

    public static final String defaultHost = "127.0.0.1";

    private TextField inputField;
    private VBox messageList;
    private Button send;
    private Button connect;
    private Socket clientSocket;
    private BufferedReader clientIn;
    private PrintWriter clientOut;
    private boolean isConnected;
    private SocketAddress socketAdress;
    private Thread clientInThread;

    public static final int PORT = 8888;

    public Messaging() {
        this.setPadding(new Insets(8, 8, 8, 8));
        isConnected = false;

        messageList = new VBox();

        var scrollPane = new ScrollPane();
        scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scrollPane.setContent(messageList);
        scrollPane.setFitToWidth(true);
        messageList.heightProperty().addListener(o -> scrollPane.setVvalue(1D));

        inputField = new TextField(defaultHost);
        inputField.setMaxWidth(Double.MAX_VALUE);

        send = new Button("Send");
        send.setDisable(true);
        connect = new Button("Connect");
        connect.setDefaultButton(true);
        connect.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (isConnected) {
                    Disconnect();
                } else {
                    try {
                        Connect();
                    } catch (Exception e1) {
                        System.out.println("Connexion impossible");
                    }
                }
            }
        });

        send.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (isConnected) {
                    String msg = inputField.getText();
                    if (msg.equals("/exit")) {
                        Disconnect();
                    } else {
                        JSONObject jsonMsg = new JSONObject();
                        if (msg.startsWith("/nickname")) {
                            jsonMsg.put("type", "nickname");
                            jsonMsg.put("nickname", msg.substring(10));
                        } else {
                            jsonMsg.put("type", "message-sent");
                            jsonMsg.put("content", msg);
                        }
                        clientOut.println(jsonMsg.toString());
                        inputField.setText("");
                    }
                }
            }
        });
        HBox buttonBox = new HBox(5, connect, send);
        AnchorPane buttonsPane = new AnchorPane(buttonBox);
        AnchorPane.setRightAnchor(buttonBox, 5.0);

        BorderPane bottomPane = new BorderPane();
        bottomPane.setCenter(inputField);
        bottomPane.setBottom(buttonsPane);

        this.setCenter(scrollPane);
        this.setBottom(bottomPane);
    }

    public void Connect() {
        socketAdress = new InetSocketAddress(inputField.getText(), PORT);
        if (!isConnected) {
            clientSocket = new Socket();
            try {
                clientSocket.connect(socketAdress);
                isConnected = true;
                System.out.println("Connected");
                connect.setText("Disconnect");
                send.setDisable(false);
                inputField.setText("");
                clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
                clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                JSONObject bvnMsg = new JSONObject(clientIn.readLine());
                String bvnMsgType = bvnMsg.getString("type");
                if (bvnMsgType.equals("notification")) {
                    String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm")
                            .format(Calendar.getInstance().getTime());
                    Text msg = new Text(timeStamp + " | " + bvnMsg.getString("content"));
                    msg.setFill(Color.RED);
                    messageList.getChildren().add(msg); // Message de bienvenue
                }
                clientInThread = new Thread(new Runnable() {
                    public void run() {
                        while (!clientSocket.isClosed()) {
                            try {
                                JSONObject jsonMsg = new JSONObject(clientIn.readLine());
                                String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm")
                                        .format(Calendar.getInstance().getTime());
                                String jsonMsgType = jsonMsg.getString("type");
                                Text msg = new Text();
                                if (jsonMsgType.equals("message-received")) {
                                    msg.setText(timeStamp + " | " + jsonMsg.getString("content"));
                                } else if (jsonMsgType.equals("notification")) {
                                    msg.setText(timeStamp + " | " + jsonMsg.getString("content"));
                                    msg.setFill(Color.RED);
                                }

                                Platform.runLater(() -> {
                                    messageList.getChildren().add(msg);
                                });
                            } catch (IOException e) {
                                System.out.println("Déconnexion");
                            }
                        }
                    }
                });
                clientInThread.start();
            } catch (IOException e1) {
                System.out.println("Connexion impossible");
            }
        }
    }

    public void Disconnect() {
        if (isConnected) {
            try {
                JSONObject jsonMsg = new JSONObject();
                jsonMsg.put("type", "disconnect");
                clientOut.println(jsonMsg.toString());
                isConnected = false;
                System.out.println("Disconnected");
                connect.setText("Connect");
                send.setDisable(true);
                inputField.setText(defaultHost);
                clientInThread.interrupt();
                clientSocket.close();
            } catch (IOException e1) {
                System.out.println("Déconnexion impossible");
            }
        }
    }
}
