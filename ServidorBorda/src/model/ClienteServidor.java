package model;

/**Classe para objetos do tipo ClienteServidor contendo atributos e métodos para os mesmos.
 * @author Nadine Cerqueira Marques
 */

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClienteServidor implements Runnable {
    private final ServidorBordaThread servidorBordaThread;
    private final Socket cliente;
    private final PrintStream ps;
    private int id;
    private String login;
    
    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * 
     * @param cliente
     * @param servidorBorda
     * @param ps
     * @throws IOException 
     */
    public ClienteServidor(Socket cliente, ServidorBordaThread servidorBorda, PrintStream ps) throws IOException {
        this.cliente = cliente;
        this.ps = ps;
        this.servidorBordaThread = servidorBorda;
    }
    
    @Override
    public void run() {
        Scanner entrada;
        try {
            entrada = new Scanner(cliente.getInputStream());

            while (entrada.hasNextLine()) {
                String mensagem = entrada.nextLine();
                servidorBordaThread.getServidorBorda().tratar(mensagem, this);
                System.out.println("Mensagem recebida: " + mensagem);
            }
            servidorBordaThread.getServidorBorda().desconectar(this);
        } catch (IOException e) {
        } catch (InterruptedException ex) {
            Logger.getLogger(ClienteServidor.class.getName()).log(Level.SEVERE, null, ex);
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
