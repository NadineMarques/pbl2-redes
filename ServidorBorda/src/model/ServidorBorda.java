package model;

/**
 * Classe para objetos do tipo ServidorBorda contendo atributos e métodos para
 * os mesmos.
 *
 * @author Nadine Cerqueira Marques
 */
import java.awt.Point;
import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorBorda implements Runnable {

    private ArrayList<SensorServidor> sensores; //lista de sensores online
    private ArrayList<MedicoServidor> medicos; //lista de médicos cadastrados
    private Socket cliente; //socket para comunicação com clientes
    private ServerSocket servidorBorda; //serverSocket para comunicação com clientes
    private PrintStream ps; //canal de saida das mensagens
    private Point ponto;
    private PrintStream mensagem;
    private Scanner entrada;
    private Socket clientSocket;
    private String msg;
    private PrintStream saida;
    private Random localizaçãoPonto;
    private int porta;

    public ServidorBorda() throws IOException {
        sensores = new ArrayList<>(); //inicialização da lista de sensores
        medicos = new ArrayList<>(); //inicialização da lista de médicos
        localizaçãoPonto = new Random();
        ponto = new Point(localizaçãoPonto.nextInt(21), localizaçãoPonto.nextInt(21));
        //fazer ponto ser randômico de 0 a 20
        System.out.println("Servidor de Borda Localizado em: " + "X:" + ponto.getX() + " Y:" + ponto.getY());
        ServidorBordaThread temp = new ServidorBordaThread(cliente, this, ps); //cria thread para o cliente genérico clienteservidor
        new Thread(temp).start(); //inicia a thread de cliente genérico
        this.porta = temp.getPorta();
        this.conectar();
        this.run();
    }

    /**
     * Conecta servidor de borda ao servidor nuvem
     *
     */
    private PrintStream conectar() throws UnknownHostException, IOException {
        clientSocket = new Socket("localhost", 3111);
        System.out.println("Servidor de Borda Conectado ao Servidor Nuvem");
        saida = new PrintStream(clientSocket.getOutputStream());
        saida.println("#CADASTRARBORDA " + clientSocket.getInetAddress().getHostAddress() + " " + porta);
        entrada = new Scanner(clientSocket.getInputStream());
        return saida;
    }
    @Override
    public void run() {
        saida.println("#LOGARBORDA " + this.clientSocket.getInetAddress().getHostAddress());
        while (entrada.hasNextLine()) {
            msg = entrada.nextLine();
            this.tratarMensagemServidor(msg);
        }
    }

    /*
    *Método responsável por decodifcar o protocolo de comunicação entre clientes e servidores.     
    *@return void 
    *@param String mensagem
    * @throws IOException, InterruptedException 
     */
    public void tratar(String mensagem, ClienteServidor clienteServidor) throws IOException, InterruptedException {
        String vetor[] = mensagem.split(" "); //retira " " da mensagem recebida e divide essa mensagem em um vetor de strings 
        String codigo = vetor[0]; //atribue código do protocolo de comunicação à váriável código

        switch (codigo) {
            case "#CONECTASENSORESNUVEM":
                for(SensorServidor s: sensores){
                    s.getClienteSensor().mandarMsg("#CONECTANUVEM");
                }
                break;
            case "#S": //cadastro de sensores
                int id = Integer.parseInt(vetor[1]);
                SensorServidor s = new SensorServidor(clienteServidor);
                clienteServidor.setId(id);
                System.out.println("Sensor " + id + " se conectou.");
                sensores.add(s);
                System.out.println("Quantidades de Sensores Conectados: " + sensores.size());
                for (MedicoServidor temp : medicos) {
                    System.out.println("Adicionando Sensores aos Médicos");
                    temp.getClienteServidor().mandarMsg("#CS " + s.getClienteSensor().getId());
                }
                break;

            case "#DS": //desconectar sensor
                id = Integer.parseInt(vetor[1]);
                desconectarSensor(id);
                break;

            case "#CM": //cadastro de medicos
                MedicoServidor m;
                String aux5[] = vetor;
                m = buscarMedico(aux5[1]);
                if (m == null) {
                    m = new MedicoServidor(clienteServidor);
                    m.setLogin(aux5[1]);
                    m.getClienteServidor().setLogin(aux5[1]);
                    m.setSenha(aux5[2]);
                    medicos.add(m);
                    clienteServidor.mandarMsg("#CADASTRADO"); //cadastrado com sucesso
                } else {
                    clienteServidor.mandarMsg("#NCADASTRADO"); //medico ja existente
                }
                break;

            case "#LM": //login de médicos
                String aux2[] = vetor;
                MedicoServidor m2 = buscarMedico(aux2[1]);
                if (m2 == null) {
                    clienteServidor.mandarMsg("#NLOGOU"); // medico n cadastrado
                } else {
                    clienteServidor.mandarMsg("#LOGOU"); // medico logou com sucesso
                    m2.setClienteServidor(clienteServidor);
                    m2.setOnline(true);
                    m2.getClienteServidor().mandarMsg("#S " + sensores.toString());
                }
                System.out.println("Quantidades de Médicos Conectados: " + (medicos.size()));
                break;

            case "#DM": //desconectar medico
                String login = vetor[1];
                desconectarMedico(login);
                break;

            case "#DADOS": //recebimento de dados do sensor
                String dados = vetor[1];
                this.atualizarDados(clienteServidor, dados);
                System.out.println("Atualizando Dados...");
                break;

            case "#SP": //selecionar paciente a ser monitorado
                String idPaciente = vetor[2];
                String aux6[] = vetor[3].split("#");
                String loginMedico = aux6[1];
                System.out.println("ID PACIENTE:" + idPaciente + "LOGIN:" + loginMedico);
                for (SensorServidor paciente : sensores) {
                    if (paciente.getClienteSensor().getId() == Integer.parseInt(idPaciente)) {
                        for (MedicoServidor medico : medicos) {
                            if (medico.getLogin().equals(loginMedico)) {
                                System.out.println("#DADOSPACIENTE " + paciente.getBatimentosCard() + " " + paciente.getPressaoSangue() + " " + paciente.getMovimento());
                                medico.getClienteServidor().mandarMsg("#DADOSPACIENTE " + paciente.getClienteSensor().getId() + " " + paciente.getBatimentosCard() + " " + paciente.getPressaoSangue() + " " + paciente.getMovimento());
                            }
                        }
                    }
                }
                break;
        }
    }

    public void tratarMensagemServidor(String mensagem) {
        String vetor[] = mensagem.split(" ", 2);
        String codigo = vetor[0];
        System.out.println("Mensagem " + mensagem);

        switch (codigo) {
            case "#CADASTRADO": //médico foi cadastrado
                System.out.println("Médico Cadastrado");
                break;
            
        }
    }

    /**
     * Método que atualiza dados dos pacientes da lista de pacientes
     *
     * @param cliente
     * @param dados
     */
    public void atualizarDados(ClienteServidor cliente, String dados) throws InterruptedException {
        String vetor[] = dados.split("-");
        char movimento = vetor[0].charAt(0);
        int batimentosCard = Integer.parseInt(vetor[1]);
        System.out.println(dados);
        String pressaoSangue = vetor[2];
        for (SensorServidor s : sensores) {
            if (s.getClienteSensor().getId() == cliente.getId()) {
                s.setMovimento(movimento);
                s.setBatimentosCard(batimentosCard);
                s.setPressaoSangue(pressaoSangue);
                this.enviarMaisPropensos(cliente, movimento, batimentosCard, pressaoSangue);

            }
        }

    }

    /**
     * Método que procura um médico
     *
     * @param login
     * @return
     */
    public MedicoServidor buscarMedico(String login) {
        MedicoServidor aux = null;
        for (MedicoServidor m : medicos) {
            if (m.getLogin().equals(login)) {
                aux = m;
            }
        }
        return aux;
    }

    /**
     * Método que procura um sensor
     *
     * @param id
     * @return
     */
    public SensorServidor buscarSensor(int id) {
        SensorServidor aux = null;
        for (SensorServidor s : sensores) {
            if (s.getClienteSensor().getId() == id) {
                aux = s;
            }
        }
        return aux;
    }

    /**
     * Método que envia dados de pacientes propensos a algum ataque cardíaco
     *
     * @param cliente
     * @param movimento
     * @param batimentosCard
     * @param pressaoSangue
     */
    public void enviarMaisPropensos(ClienteServidor cliente, char movimento, int batimentosCard, String pressaoSangue) throws InterruptedException {
        
        System.out.println("Enviando pacientes propensos...");
        while(true) {
            if (batimentosCard < 40 && movimento == 'R') {
                System.out.println("#PACIENTEPROPENSO " + cliente.getId() + " " + batimentosCard + " " + pressaoSangue + " " + movimento);
                saida.println("#PACIENTEPROPENSO " + cliente.getId());
            } else if (batimentosCard < 40 && movimento == 'M') {
                System.out.println("#PACIENTEPROPENSO " + cliente.getId() + " " + batimentosCard + " " + pressaoSangue + " " + movimento);
                saida.println("#PACIENTEPROPENSO " + cliente.getId());
            } else if (batimentosCard > 100 && movimento == 'R') {
                System.out.println("#PACIENTEPROPENSO " + cliente.getId() + " " + batimentosCard + " " + pressaoSangue + " " + movimento);
                saida.println("#PACIENTEPROPENSO " + cliente.getId());
            } else {
                saida.println("#RETIRAR " + cliente.getId());//caso nenhuma das condições seja satisfeita o paciente não está em risco e deve ser retirado da tabela
            }
        Thread.sleep(5000);
        }
    }

    /**
     * Método que desconecta tanto sensores quanto médicos
     *
     * @param cliente
     */
    public void desconectar(ClienteServidor cliente) {
        if (cliente.getId() == 0) { //id==0 significa que o cliente é médico
            for (MedicoServidor m : medicos) {
                if (m.getClienteServidor().getLogin().equals(cliente.getLogin())) {
                    m.setOnline(false);
                    System.out.println("Médico Desconectou");
                }
            }
        } else { //caso id!=0 o cliente é um sensor
            SensorServidor aux = null;
            for (SensorServidor s : sensores) {
                if (s.getClienteSensor().getId() == cliente.getId()) {
                    aux = s;
                }
            }
            if (aux == null) {
                return;
            }
            sensores.remove(aux);//remove o sensor encontrado
            System.out.println("Sensor Desconectou");
            for (MedicoServidor temp : medicos) { //remove o sensor também da lista de médicos
                temp.getClienteServidor().mandarMsg("#DS " + cliente.getId());
                temp.getClienteServidor().mandarMsg("#RETIRAR " + cliente.getId());
            }
        }
    }

    /**
     * Método que desconecta médicos
     *
     * @param login
     */
    public void desconectarMedico(String login) {
        for (MedicoServidor m : medicos) {
            if (m.getClienteServidor().getLogin().equals(login)) {
                m.setOnline(false);
                System.out.println("Médico " + login + " Desconectou");
            }
        }
    }

    public void desconectarSensor(int id) {
        SensorServidor aux = null;
        for (SensorServidor s : sensores) {
            if (s.getClienteSensor().getId() == id) {
                aux = s;
            }
        }
        if (aux == null) {
            return;
        }
        sensores.remove(aux);//remove o sensor encontrado
        System.out.println("Sensor " + id + " Desconectou");
        for (MedicoServidor temp : medicos) { //remove o sensor também da lista de médicos
            temp.getClienteServidor().mandarMsg("#DS " + id);
            temp.getClienteServidor().mandarMsg("#RETIRAR " + id);
        }
    }

}
