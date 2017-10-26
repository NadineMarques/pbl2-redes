package model;

/**
 * Classe para objetos do tipo ServidorNuvem contendo atributos e métodos para
 * os mesmos.
 *
 * @author Nadine Cerqueira Marques
 */
import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorNuvem {

    private final ArrayList<ServidorBorda> servidoresBorda; //lista de sensores online
    private final ArrayList<MedicoServidor> medicos; //lista de sensores online
    private Socket cliente; //socket para comunicação com clientes
    private ServerSocket servidor; //serverSocket para comunicação com clientes
    private PrintStream ps; //canal de saida das mensagens

    public ServidorNuvem() {
        servidoresBorda = new ArrayList<>(); //inicialização da lista de sensores
        //verificar arquivo com lista de servidores de borda
    }

    /*
    *Método responsável por iniciar o servidor e aceitar novos clientes sensores e médicos    
    *@return void 
    *@param
    * @throws IOException 
     */
    public void iniciarServidor() throws IOException {
        servidor = new ServerSocket(3111); //criação de serversocket com a porta 3111
        System.out.println("Servidor Online");

        while (true) { //faz com que o servidor se conecte a novos clientes durante o tempo que estiver conectado
            cliente = servidor.accept(); //aceita novos clientes
            this.ps = new PrintStream(cliente.getOutputStream()); //atribue o canal de saída do cliente para que o servidor possa enviar mensagens a ele
            System.out.println("Cliente: " + cliente.getInetAddress().getHostAddress() + " se conectou");
            ClienteServidor temp = new ClienteServidor(cliente, this, ps); //cria thread para o cliente genérico clienteservidor
            new Thread(temp).start(); //inicia a thread de cliente genérico
        }
    }

    /*
    *Método responsável por decodifcar o protocolo de comunicação entre clientes e servidores.     
    *@return void 
    *@param String mensagem
    * @throws IOException, InterruptedException 
     */
    public void tratar(String mensagem, ClienteServidor clienteServidor) throws IOException, InterruptedException {
        String vetor[] = mensagem.split(" ");//retira " " da mensagem recebida e divide essa mensagem em um vetor de strings 
        String codigo = vetor[0];//atribue código do protocolo de comunicação à váriável código

        switch (codigo) {
            case "#S": //cadastro de sensores
                int id = Integer.parseInt(vetor[1]);
                SensorServidor s = new SensorServidor(clienteServidor);
                clienteServidor.setId(id);
                break;

            case "#M": // cadastro de medicos
                MedicoServidor m;
                break;

            case "#P": //selecionar paciente a ser monitorado
                String idPaciente = vetor[2];
                String aux6[] = vetor[3].split("#");
                String loginMedico = aux6[1];
                System.out.println("ID PACIENTE:" + idPaciente + "LOGIN:" + loginMedico);
                /*for (SensorServidor paciente : sensores) {
                    if (paciente.getClienteSensor().getId() == Integer.parseInt(idPaciente)) {
                        for (MedicoServidor medico : medicos) {
                            if (medico.getLogin().equals(loginMedico)) {
                                System.out.println("#DADOSPACIENTE " + paciente.getBatimentosCard() + " " + paciente.getPressaoSangue() + " " + paciente.getMovimento());
                                medico.getClienteServidor().mandarMsg("#DADOSPACIENTE " + paciente.getClienteSensor().getId() + " " + paciente.getBatimentosCard() + " " + paciente.getPressaoSangue() + " " + paciente.getMovimento());
                            }
                        }
                    }
                }*/
                break;
        }
    }
    
    /**
     * Método que envia dados de pacientes propensos a algum ataque cardíaco
     * @param dadosPropenso
     */
    public void enviarMaisPropensos(String dadosPropenso) {
        System.out.println("Dados do paciente propenso: " + dadosPropenso);
        //String idPaciente = vetor[2];
        //String batimentosCard = vetor[3];
        //String pressaoSangue = vetor[4];
        //char movimento = vetor[5];
        for (MedicoServidor m : medicos) {
            System.out.println("#PACIENTESPROPENSOS " + cliente.getId() + " " + batimentosCard + " " + pressaoSangue + " " + movimento);
            m.getClienteServidor().mandarMsg("#PACIENTESPROPENSOS " + cliente.getId() + " " + batimentosCard + " " + pressaoSangue + " " + movimento);
        }
    }

    /**
     * Método que desconecta tanto sensores quanto médicos
     *
     * @param cliente
     */
    public void desconectar(ClienteServidor cliente) {
        if (cliente.getId() == 0) {
            Iterable<MedicoServidor> medicos = null;
            //id==0 significa que o cliente é médico
            for (MedicoServidor m : medicos) {
                if (m.getClienteServidor().getLogin().equals(cliente.getLogin())) {
                    m.setOnline(false);
                    System.out.println("Médico Desconectou");
                }
            }
        }
    }
}
