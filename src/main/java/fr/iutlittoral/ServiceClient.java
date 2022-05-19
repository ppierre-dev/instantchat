package fr.iutlittoral;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONObject;

public class ServiceClient implements Runnable {

    private int id;
    private Socket s;
    private Diffusion d;
    private String nickname;

    public ServiceClient(Socket s, Diffusion d, int clientId) {
        this.id = clientId;
        this.s = s;
        this.d = d;
        this.nickname = "";
    }

    @Override
    public void run() {
        d.addNewClient(this);
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
            while (!this.s.isClosed()) {
                String msg = input.readLine();
                if (msg != null) {
                    JSONObject jsonMsg = new JSONObject(msg);
                    String msgType = jsonMsg.getString("type");
                    if (msgType.equals("disconnect")) {
                        d.removeClient(this);
                        d.diffuseToAllClients("Client " + id + " s'est deconnecté", true);
                    } else if (msgType.equals("message-sent")) {
                        String msgContent = jsonMsg.getString("content");
                        String msgToSend = new String();
                        if (!this.nickname.equals("")) {
                            msgToSend = this.nickname + " : " + msgContent;
                        } else {
                            msgToSend = "Client " + id + " : " + msgContent;
                        }
                        d.diffuseToAllClients(msgToSend, false);
                    } else if (msgType.equals("nickname")) {
                        String newNickname = jsonMsg.getString("nickname");
                        this.sendMessageToClient("Vous venez de changer votre pseudo en : " + newNickname, true);
                        this.setNickname(newNickname);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToClient(String msg, boolean isNotif) {
        try {
            JSONObject jsonMsg = new JSONObject();
            if (isNotif) {
                jsonMsg.put("type", "notification");
                jsonMsg.put("content", "Système : " + msg);
            } else {
                jsonMsg.put("type", "message-received");
                jsonMsg.put("content", msg);
                jsonMsg.put("author", this.id);
            }

            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            out.println(jsonMsg.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getClientId() {
        return id;
    }

    public Socket getClientSocket() {
        return s;
    }

}
