package model;

/**
 * Classe para objetos do tipo ServidorNuvem contendo atributos e métodos para
 * os mesmos.
 *
 * @author Nadine Cerqueira Marques
 */
import java.awt.Point;
import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorNuvem {

    private ArrayList<BordaServidor> servidoresBorda;
    private ArrayList<SensorServidor> sensores; //lista de sensores online
    private ArrayList<SensorServidor> pacientesPropensos;
    private ArrayList<MedicoServidor> medicos; //lista de médicos cadastrados
    private Socket cliente; //socket para comunicação com clientes
    private ServerSocket servidor; //serverSocket para comunicação com clientes
    private PrintStream ps; //canal de saida das mensagens

    public ServidorNuvem() {
        sensores = new ArrayList<>(); //inicialização da lista de sensores
        medicos = new ArrayList<>(); //inicialização da lista de médicos
        servidoresBorda = new ArrayList<>();
    }

    /*
    *Método responsável por iniciar o servidor e aceitar novos clientes sensores e médicos    
    *@return void 
    *@param
    * @throws IOException 
     */
    public void iniciarServidor() throws IOException {
        servidor = new ServerSocket(3111); //criação de serversocket com a porta 3111
        System.out.println("Servidor Nuvem Online");
        sensores = new ArrayList<>();

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
            case "#CADASTRARBORDA":
                System.out.println("Cadastrar Servidor de Borda: " + mensagem);
                String ip = vetor[1];
                int porta = Integer.parseInt(vetor[2]);
                BordaServidor bordaServidor = new BordaServidor(clienteServidor);
                bordaServidor.getClienteBordaServidor().setIp(ip);
                bordaServidor.getClienteBordaServidor().setPorta(porta);
                servidoresBorda.add(bordaServidor);
                System.out.println("Servidor de Borda " + bordaServidor.getClienteBordaServidor().getIp() + " com a porta " + porta + " se conectou.");

                for (SensorServidor s : sensores) {
                    BordaServidor auxBordaServidor = localizarServidorBordaProximo(s.getClienteSensor().getPonto());
                    if (auxBordaServidor != null) {
                        auxBordaServidor.getSensores().add(s);
                        s.getClienteSensor().mandarMsg("#DADOSBORDA " + auxBordaServidor.getClienteBordaServidor().getIp() + " " + auxBordaServidor.getClienteBordaServidor().getPorta());
                    }
                }

                break;

            case "#LOGARBORDA":
                ip = vetor[1];
                BordaServidor b = buscarServidorBorda(ip);
                if (!b.equals(null)) {
                    b.setOnline(true);
                }
                break;
            case "#DESCONECTARBORDA":
                ip = vetor[1];
                b = buscarServidorBorda(ip);
                if (!b.equals(null)) {
                    b.getClienteBordaServidor().mandarMsg("#CONECTASENSORESNUVEM");
                    b.setOnline(false);
                }
                break;

            case "#S": //cadastro de sensores
                String dadosSensor[] = vetor;
                int id = Integer.parseInt(dadosSensor[1]);
                SensorServidor s = new SensorServidor(clienteServidor);
                s.getClienteSensor().setId(id);
                System.out.println("PONTO: " + dadosSensor[2] + " " + dadosSensor[3]);
                s.getClienteSensor().setPonto(Double.parseDouble(dadosSensor[2]), Double.parseDouble(dadosSensor[3]));
                System.out.println("Sensor " + id + " se conectou.");
                sensores.add(s);
                System.out.println("Quantidades de Sensores Conectados: " + sensores.size());

                BordaServidor auxBordaServidor = localizarServidorBordaProximo(s.getClienteSensor().getPonto());
                if (auxBordaServidor != null) {
                    System.out.println("Servido de Borda: " + auxBordaServidor.isOnline());
                    s.getClienteSensor().mandarMsg("#DADOSBORDA " + auxBordaServidor.getClienteBordaServidor().getIp() + " " + auxBordaServidor.getClienteBordaServidor().getPorta());
                }
                break;

            case "#CM": // cadastro de medicos
                MedicoServidor m;
                String aux[] = vetor;
                m = buscarMedico(aux[1]);
                if (m == null) {
                    m = new MedicoServidor(clienteServidor);
                    m.setLogin(aux[1]);
                    m.getClienteServidor().setLogin(aux[1]);
                    m.setSenha(aux[2]);
                    m.getClienteServidor().setPonto(Double.parseDouble(aux[3]), Double.parseDouble(aux[4]));
                    m.setSenha(aux[2]);
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
                    if(!this.sensores.isEmpty())
                        m2.getClienteServidor().mandarMsg("#S " + sensores.toString());
                }
                System.out.println("Quantidades de Médicos Conectados: " + (medicos.size()));

                break;

            case "#DADOS": //recebimento de dados do sensor
                String dados = vetor[1];
                this.atualizarDados(clienteServidor, dados);
                System.out.println("Atualizando Dados...");
                break;
                
            case "#PACIENTEPROPENSO":
                String idPropenso = vetor[1];
                for(MedicoServidor medicos: medicos){
                    medicos.getClienteServidor().mandarMsg("#PACIENTEPROPENSO " + idPropenso);
                }
                break;

            case "#SP": //selecionar paciente a ser monitorado
                String idPaciente = vetor[2];
                String dadosMedico[] = vetor[3].split("#");
                String loginMedico = dadosMedico[1];
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

    public BordaServidor localizarServidorBordaProximo(Point ponto) {
        BordaServidor aux = null;
        Point pontoBorda = null;
        double distancia = 0, distanciaMenor = 1000000000;
        for (BordaServidor bs : servidoresBorda) {
            pontoBorda = bs.getClienteBordaServidor().getPonto();
            distancia = Math.sqrt(Math.pow((pontoBorda.getX() - ponto.getX()), 2)
                    + Math.pow((pontoBorda.getY() - ponto.getY()), 2));

            if (distancia < distanciaMenor) {
                aux = bs;
                distanciaMenor = distancia;
            }
        }
        return aux;
    }

    /**
     * Método que atualiza dados dos pacientes da lista de pacientes
     *
     * @param cliente
     * @param dados
     */
    public void atualizarDados(ClienteServidor cliente, String dados) {
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
     * Método que procura um servidor de borda
     *
     * @param ip
     * @return
     */
    public BordaServidor buscarServidorBorda(String ip) {
        BordaServidor aux = null;
        for (BordaServidor s : servidoresBorda) {
            if (s.getClienteBordaServidor().getIp().equals(ip)) {
                aux = s;
            }
        }
        return aux;
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
                    for (BordaServidor s : servidoresBorda) {
                        s.getClienteBordaServidor().mandarMsg("#DM " + cliente.getLogin());
                    }
                    m.setOnline(false);
                    System.out.println("Médico Desconectou");
                }
            }
        } else { //caso id!=0 o cliente é um sensor
            SensorServidor aux = null;
            for (SensorServidor s : sensores) {
                if (s.getClienteSensor().getId() == cliente.getId()) {
                    aux = s;
                    for (BordaServidor servidores : servidoresBorda) {
                        servidores.getClienteBordaServidor().mandarMsg("#DS " + cliente.getId());
                    }
                }
            }
            if (aux == null) {
                return;
            }
            sensores.remove(aux);//remove o sensor encontrado
            System.out.println("Sensor Desconectou");
            System.out.println("Quantidades de Sensores Conectados: " + sensores.size());
            for (MedicoServidor temp : medicos) { //remove o sensor também da lista de médicos
                temp.getClienteServidor().mandarMsg("#DS " + cliente.getId());
                temp.getClienteServidor().mandarMsg("#RETIRAR " + cliente.getId());
            }
        }
    }
    public void desconectarServidorBorda(ClienteServidor clienteBordaServidor) {
        for (BordaServidor s : servidoresBorda) {
            if (s.getClienteBordaServidor().getIp().equals(clienteBordaServidor.getIp())) {
                s.setOnline(false);
                System.out.println("Médico Desconectou");
            }
        }
    }

}
