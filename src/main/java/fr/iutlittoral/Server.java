package fr.iutlittoral;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONObject;

public class Server {
    private final static int PORT = 8888;
    private static int clientID = 0;

    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(PORT);
            Diffusion d = new Diffusion();
            while (!ss.isClosed()) {
                Socket s = ss.accept();
                PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                ServiceClient service = new ServiceClient(s, d, clientID);
                Thread th = new Thread(service);
                th.start();
                JSONObject jsonMsg = new JSONObject();
                jsonMsg.put("type", "notification");
                jsonMsg.put("content", "Bienvenue sur notre application de chat instantané, l'ID n°" + clientID
                        + " vous a été attribué (Vous seul pouvez voir ce message)");
                out.println(jsonMsg.toString());
                d.diffuseToAllClients("Client " + clientID + " connecté", true);
                clientID++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
