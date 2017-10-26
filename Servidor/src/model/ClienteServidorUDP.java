package model;

/**.
 * @author Nadine Cerqueira Marques
 */

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClienteServidorUDP implements Runnable {

    private char movimento;
    private int id, batimentosCard, pressaoSangue;
    private PrintStream ps;
    private final Socket cliente;
    private final Servidor servidor;
    private final DatagramSocket servidorDatagram;
    private int porta, numConn = 1;

    public ClienteServidorUDP(Socket cliente, Servidor servidor, DatagramSocket servidorDatagram) throws IOException {
        this.cliente = cliente;
        this.servidor = servidor;
        this.ps = new PrintStream(cliente.getOutputStream());
        this.servidorDatagram = new DatagramSocket(cliente.getPort());
        this.ps = new PrintStream(cliente.getOutputStream());
        this.porta = cliente.getPort();
    }

    @Override
    public void run() {
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];

        while (true) {
            try {
                DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
                
                System.out.println("Esperando por datagrama UDP na porta " + porta);
                
                servidorDatagram.receive(receivePacket);
                
                System.out.print("Datagrama UDP [" + numConn + "] recebido...");

                String sentence = new String(receivePacket.getData());
                
                System.out.println(sentence);

                InetAddress IPAddress = receivePacket.getAddress();

                int port = receivePacket.getPort();

                String capitalizedSentence = sentence.toUpperCase();

                sendData = capitalizedSentence.getBytes();

                DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length, IPAddress, port);

                System.out.print("Enviando " + capitalizedSentence + "...");

                servidorDatagram.send(sendPacket);
                
                System.out.println("OK\n");

            } catch (IOException ex) {
                Logger.getLogger(ClienteServidorUDP.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public int getId() {
        return id;
    }

    public char getMovimentos() {
        return this.movimento;
    }

    public void setMovimentos(char movimento) {
        this.movimento = movimento;
    }

    public int getBatimentosCard() {
        return batimentosCard;
    }

    public void setBatimentosCard(int batimentosCard) {
        this.batimentosCard = batimentosCard;
    }

    public int getPressaoSangue() {
        return pressaoSangue;
    }

    public void setPressaoSangue(int pressaoSangue) {
        this.pressaoSangue = pressaoSangue;
    }

    public void mandarMsg(String msg) {
        ps.println(msg);
    }
}
