package model;

/**Classe para objetos do tipo ClienteMedico contendo atributos e métodos para os mesmos.
 * @author Nadine Cerqueira Marques
*/

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import view.JanelaMedico;

public class ClienteMedico implements Runnable {

    private final String ip;
    private final int porta;
    private final String login;
    private final String senha;
    private final JanelaMedico jMedico;
    private PrintStream mensagem;
    private Scanner entrada;
    /**
     * Construtor
     * @param ip
     * @param porta
     * @param login
     * @param senha 
     */
    public ClienteMedico(String ip, int porta, String login, String senha) {
        this.ip = ip;
        this.porta = porta;
        this.login = login;
        this.senha = senha;
        jMedico = new JanelaMedico(this);
    }
    /**
     * Conecta médico ao servidor
     * @return
     * @throws UnknownHostException
     * @throws IOException 
     */
    private PrintStream conectar() throws UnknownHostException, IOException {
        Socket clientSocket = new Socket(ip, porta);
        System.out.println("Médico Conectado");
        PrintStream saida = new PrintStream(clientSocket.getOutputStream());
        saida.println("#CM " + login + " " + senha);
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
            Logger.getLogger(ClienteMedico.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void tratarMensagemServidor(String mensagem) {
        String vetor[] = mensagem.split(" ", 2);
        String codigo = vetor[0];
        DefaultListModel list;
        System.out.println("Mensagem " + mensagem);

        switch (codigo) {
            case "#S": //sensor se conectou ao servidor enquanto o médico está online e precisa ser mostrado na tela
                String lista = vetor[1];
                lista = lista.replace('[', ' ');
                lista = lista.replace(']', ' ');
                System.out.println("Lista: " + lista);
                String temp[] = lista.split(",");
                list = new DefaultListModel();

                jMedico.listaSensores().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        if (!list.isEmpty()) {
                            if (!e.getValueIsAdjusting()) {
                                selecionarPaciente((String) jMedico.listaSensores().getSelectedValue());
                            }
                        }
                    }
                });

                for (int i = 0; i < temp.length; i++) {
                    list.addElement(temp[i]);
                }
                jMedico.listaSensores().setModel(list);
                break;
            case "#CADASTRADO": //médico foi cadastrado
                System.out.println("Médico Cadastrado");
                break;
            case "#NCADASTRADO"://médico não foi cadastrado
                System.out.println("Médico Já Cadastrado");
                break;

            case "#LOGOU"://médico logou
                System.out.println("Médico Online");
                break;
            case "#NLOGOU"://médico não logou
                System.out.println("Médico Offline");
                break;

            case "#CS": //sensor se conectou ao sistema antes de o médico estar online. Então a lista de sensores é percorrida a fim de apresentar suas informações na tela
                String id = vetor[1];
                System.out.println(id);
                System.out.println("Sensor Conectando Id " + id);
                list = (DefaultListModel) jMedico.listaSensores().getModel();
                list.addElement(id);
                break;

            case "#DS": //sensor foi desconectado
                id = vetor[1];
                DefaultListModel list2 = (DefaultListModel) jMedico.listaSensores().getModel();
                for (int i = 0; i < list2.size(); i++) {
                    if (list2.getElementAt(i).toString().trim().equals(id)) {
                        System.out.println("Removendo Sensor: " + list2.getElementAt(i));
                        list2.removeElementAt(i);
                        jMedico.listaSensores().setModel(list2);
                    }
                }
                DefaultTableModel tabPacientes = (DefaultTableModel) jMedico.getTablePaciente().getModel();
                for (int i = 0; i < tabPacientes.getRowCount(); i++) {
                    if (id.equals(tabPacientes.getValueAt(i, 0))) {
                        tabPacientes.setValueAt("", i, 0);
                        tabPacientes.setValueAt("", i, 1);
                        tabPacientes.setValueAt("", i, 2);
                        tabPacientes.setValueAt("", i, 3);
                        return;
                    }
                }
                break;

            case "#PACIENTESPROPENSOS": //recebe do servidor dados de paciente propenso
                String aux[] = vetor[1].split(" ");
                String dadosPropenso = "Paciente - Id:" + aux[0] + "# Batimentos: " + aux[1] + "  Pressão Sanguínea: " + aux[2] + "  Movimento: " + aux[3];
                System.out.println("Paciente Propenso: " + dadosPropenso);
                DefaultTableModel tabPropensos;
                tabPropensos = (DefaultTableModel) jMedico.getTablePropensos().getModel();

                for (int i = 0; i < tabPropensos.getRowCount(); i++) {
                    if (aux[0].equals(tabPropensos.getValueAt(i, 0))) {
                        tabPropensos.setValueAt(aux[1], i, 1);
                        tabPropensos.setValueAt(aux[2], i, 2);
                        tabPropensos.setValueAt(aux[3], i, 3);
                        return;
                    }
                }
                tabPropensos.addRow(new Object[]{
                    aux[0], aux[1], aux[2], aux[3]
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
     * Envia ao servidor a mensagem que o informa qual paciente foi selecionado na lista
     * @param id 
     */
    public void selecionarPaciente(String id) {
        mensagem.println("#SP " + "#" + id + "#" + login);
        System.out.println("PACIENTE SELECIONADO: " + id);

    }
}
