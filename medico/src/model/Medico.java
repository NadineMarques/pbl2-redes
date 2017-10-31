package model;

/**
 * Classe para objetos do tipo Medico contendo atributos e métodos para os
 * mesmos.
 *
 * @author Nadine Cerqueira Marques
 */
import java.awt.Point;
import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import view.JanelaMedico;

public class Medico implements Runnable {

    private final String ip;
    private int porta, portaBorda;
    private final String login;
    private final String senha;
    private final JanelaMedico jMedico;
    private PrintStream mensagem;
    private Scanner entrada;
    private Point ponto;
    private String ipBorda;
    private Socket clientSocket;
    private String lista = null;

    /**
     * Construtor
     *
     * @param ip
     * @param porta
     * @param login
     * @param senha
     */
    public Medico(String ip, int porta, String login, String senha) {
        this.ip = ip;
        this.porta = porta;
        this.login = login;
        this.senha = senha;
        Random localizaçãoPonto = new Random();
        ponto = new Point(localizaçãoPonto.nextInt(21), localizaçãoPonto.nextInt(21));
        jMedico = new JanelaMedico(this);
    }

    /**
     * Conecta médico ao servidor
     *
     * @return
     * @throws UnknownHostException
     * @throws IOException
     */
    private PrintStream conectar() throws UnknownHostException, IOException {
        clientSocket = new Socket(ip, porta);
        System.out.println("Médico Conectado ao Servidor " + ip);
        PrintStream saida = new PrintStream(clientSocket.getOutputStream());
        saida.println("#CM " + login + " " + senha + " " + ponto.getX() + " " + ponto.getY());
        entrada = new Scanner(clientSocket.getInputStream());

        return saida;
    }

    @Override
    public void run() {
        try {
            mensagem = this.conectar();
            mensagem.println("#LM " + login + " " + senha);
            while (entrada.hasNextLine()) {
                String mensagem = entrada.nextLine();
                this.tratarMensagemServidor(mensagem);
            }
        } catch (IOException ex) {
            Logger.getLogger(Medico.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void tratarMensagemServidor(String mensagemServidor) throws IOException {
        String vetor[] = mensagemServidor.split(" ");
        String codigo = vetor[0];
        
        System.out.println("Mensagem " + mensagemServidor);

        switch (codigo) {
            case "#PACIENTEPROPENSO": //recebe do servidor dados de paciente propenso
                String aux[] = vetor[1].split(" ");
                String dadosPropenso = "Paciente - Id:" + aux[0];
                System.out.println("Paciente Propenso: " + dadosPropenso);
                DefaultTableModel tabPropensos;
                tabPropensos = (DefaultTableModel) jMedico.getTablePropensos().getModel();

                for (int i = 0; i < tabPropensos.getRowCount(); i++) {
                    if (aux[0].equals(tabPropensos.getValueAt(i, 0))) {
                        tabPropensos.setValueAt(aux[0], i, 0);
                        return;
                    }
                }
                tabPropensos.addRow(new Object[]{
                    aux[0]
                });
                break;

            case "#RETIRAR": //retira paciente da tabela de mais propensos caso seus dados não indiquem mais riscos
                String aux2[] = vetor[1].split(" ");
                System.out.println("Dados Paciente Propenso: " + aux2[0]);
                tabPropensos = (DefaultTableModel) jMedico.getTablePropensos().getModel();
                for (int i = 0; i < tabPropensos.getRowCount(); i++) {
                    if (aux2[0].equals(tabPropensos.getValueAt(i, 0))) {
                        tabPropensos.removeRow(i);
                        return;
                    }
                }
                break;

            case "#DADOSBORDA":
                this.ipBorda = vetor[1];
                this.porta = Integer.parseInt(vetor[2]);
                clientSocket.close();
                System.out.println("Ip: " + ipBorda + " " + " Porta: " + porta);
                mensagem = this.conectar();
                break;

            case "#DADOSPACIENTE": //recebe do servidor dados do paciente selecionado na lista
                String aux4[] = vetor[1].split(" ");
                String dadosPaciente = "Paciente - Id:" + vetor[0] + " Batimentos: " + aux4[1] + "  Pressão Sanguínea: " + aux4[2] + "  Movimento: " + aux4[3];
                System.out.println("PACIENTE SELECIONADO:  " + dadosPaciente);

                DefaultTableModel tablePaciente;
                tablePaciente = (DefaultTableModel) jMedico.getTablePaciente().getModel();
                for (int i = 0; i < tablePaciente.getRowCount(); i++) {
                    if (vetor[0].equals(tablePaciente.getValueAt(i, 0))) {
                        tablePaciente.setValueAt(aux4[1], i, 1);
                        tablePaciente.setValueAt(aux4[2], i, 2);
                        tablePaciente.setValueAt(aux4[3], i, 3);
                        return;
                    } else {
                        tablePaciente.removeRow(i);
                        return;
                    }
                }
                tablePaciente.addRow(new Object[]{
                    aux4[0], aux4[1], aux4[2], aux4[3]
                });
                break;
        }
    }

    /**
     * Envia ao servidor a mensagem que o informa qual paciente foi selecionado
     * na lista
     *
     * @param id
     */
    public void selecionarPaciente(String id) {
        mensagem.println("#SP " + "#" + id + "#" + login);
        System.out.println("PACIENTE SELECIONADO: " + id);
    }
}
