package model;

/**
 * Classe para objetos do tipo ClienteServidor contendo atributos e métodos para
 * os mesmos.
 *
 * @author Nadine Cerqueira Marques
 */
import java.awt.Point;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClienteServidor implements Runnable {

    private ServidorNuvem servidor;
    private final Socket cliente;
    private final PrintStream ps;
    private int id;
    private String login;
    private String ip;
    private Point ponto;
    private int porta;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

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

    public Point getPonto() {
        return ponto;
    }

    /**
     *
     * @param cliente
     * @param servidor
     * @param ps
     * @throws IOException
     */
    public ClienteServidor(Socket cliente, ServidorNuvem servidor, PrintStream ps) throws IOException {
        this.cliente = cliente;
        this.servidor = servidor;
        this.ps = ps;
        this.ponto = new Point();
    }

    @Override
    public void run() {
        Scanner entrada;
        try {
            entrada = new Scanner(cliente.getInputStream());

            while (entrada.hasNextLine()) {
                String mensagem = entrada.nextLine();
                servidor.tratar(mensagem, this);
                System.out.println("Mensagem recebida: " + mensagem);
            }
            servidor.desconectar(this);
        } catch (IOException e) {
        } catch (InterruptedException ex) {
            Logger.getLogger(ClienteServidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Envia mensagem ao cliente
     *
     * @param msg
     */
    public void mandarMsg(String msg) {
        ps.println(msg);
        ps.flush();
    }

    void setPonto(double x, double y) {
        ponto.setLocation(x, y);
    }

    public int getPorta() {
        return porta;
    }

    public void setPorta(int porta) {
        this.porta = porta;
    }
}
