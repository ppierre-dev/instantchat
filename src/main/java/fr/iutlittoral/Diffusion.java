package fr.iutlittoral;

import java.util.ArrayList;

public class Diffusion {
    private ArrayList<ServiceClient> connectedClients;

    public Diffusion() {
        connectedClients = new ArrayList<ServiceClient>();
    }

    public ArrayList<ServiceClient> getConnectedClients() {
        return this.connectedClients;
    }

    public void addNewClient(ServiceClient client) {
        this.connectedClients.add(client);
    }

    public void removeClient(ServiceClient client) {
        this.connectedClients.remove(client);
    }

    public void diffuseToAllClients(String msg, boolean isNotif) {
        for (ServiceClient client : connectedClients) {
            client.sendMessageToClient(msg, isNotif);
        }
    }
}
