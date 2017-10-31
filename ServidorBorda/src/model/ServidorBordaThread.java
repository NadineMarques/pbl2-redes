/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nady_
 */
public class ServidorBordaThread implements Runnable {

    private Socket cliente; //socket para comunicação com clientes
    private PrintStream ps;
    private ServidorBorda servidor;
    private Random portaRandom;
    private int porta;

    public ServidorBordaThread(Socket cliente, ServidorBorda servidor, PrintStream ps) throws IOException {
        this.cliente = cliente;
        this.servidor = servidor;
        this.ps = ps;
        portaRandom = new Random();
        int s = 60000; //limite superior aleatório
        int i = 50000; //limite inferior aleatório
        porta = (portaRandom.nextInt(s - i + 1) + i);
    }

    public ServidorBorda getServidorBorda() {
        return this.servidor;
    }

    @Override
    public void run() {
        ServerSocket servidorBorda;
        try {

            servidorBorda = new ServerSocket(porta); //criação de serversocket com a porta 3111
            System.out.println("Servidor de Borda Online");

            while (true) { //faz com que o servidor se conecte a novos clientes durante o tempo que estiver conectado
                cliente = servidorBorda.accept(); //aceita novos clientes
                this.ps = new PrintStream(cliente.getOutputStream()); //atribue o canal de saída do cliente para que o servidor possa enviar mensagens a ele
                System.out.println("Cliente: " + cliente.getInetAddress().getHostAddress() + " se conectou");
                ClienteServidor temp = new ClienteServidor(cliente, this, ps); //cria thread para o cliente genérico clienteservidor
                new Thread(temp).start(); //inicia a thread de cliente genérico
            }
        } catch (IOException ex) {
            Logger.getLogger(ServidorBordaThread.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public int getPorta() {
        return this.porta;
    }

}
