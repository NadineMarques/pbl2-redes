package model;

import java.util.ArrayList;

/**
 * Classe para objetos do tipo BordaServidor contendo atributos e métodos para
 * os mesmos.
 *
 * @author Nadine Cerqueira Marques
 */
public class BordaServidor {

    private final ClienteServidor clienteBordaServidor;
    private boolean online = false;
    private ArrayList<SensorServidor> sensores;
    
    public ArrayList getSensores(){
        return sensores;
    }

    public void setSensores(ArrayList<SensorServidor> sensores) {
        this.sensores = sensores;
    }
    
    /**
     * Método que informa se o médico atual está online
     *
     * @return online - Status do Médico
     */
    public boolean isOnline() {
        return online;
    }

    /**
     * Método que seta o status do médico
     *
     * @param online
     */
    public void setOnline(boolean online) {
        this.online = online;
    }

    /**
     * Construtor
     *
     * @param clienteBordaServidor
     */
    public BordaServidor(ClienteServidor clienteBordaServidor) {
        this.clienteBordaServidor = clienteBordaServidor;
        this.online = true;
    }

    public ClienteServidor getClienteBordaServidor() {
        return clienteBordaServidor;
    }

    /**
     * Método para retornar id do sensor
     *
     * @return
     */
    @Override
    public String toString() {
        return "" + (clienteBordaServidor.getIp());
    }
}
