package model;

/**Classe para objetos do tipo ClienteBordaServidor contendo atributos e métodos para os mesmos.
 * @author Nadine Cerqueira Marques
 */

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClienteBordaServidor implements Runnable {
    private final ServidorNuvem servidor;
    private final Socket cliente;
    private final PrintStream ps;
    private String ip;
    
    public String getIp() {
        return ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }
    /**
     * 
     * @param cliente
     * @param servidor
     * @param ps
     * @throws IOException 
     */
    public ClienteBordaServidor(Socket cliente, ServidorNuvem servidor, PrintStream ps) throws IOException {
        this.cliente = cliente;
        this.servidor = servidor;
        this.ps = ps;
    }
    
    @Override
    public void run() {
        Scanner entrada;
        try {
            entrada = new Scanner(cliente.getInputStream());

            while (entrada.hasNextLine()) {
                String mensagem = entrada.nextLine();
                //servidor.tratar(mensagem, this);
                System.out.println("Mensagem recebida: " + mensagem);
            }
            servidor.desconectar(this);
        } catch (IOException e) {
        }
    }
    /**
     * Envia mensagem ao cliente
     * @param msg 
     */
    public void mandarMsg(String msg) {
        ps.println(msg);
        ps.flush();
    }
}
