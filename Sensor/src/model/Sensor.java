package model;

/**
 * Classe para objetos do tipo Sensor contendo atributos e métodos para os
 * mesmos.
 *
 * @author Nadine Cerqueira Marques
 */
import java.awt.Point;
import java.io.*;
import static java.lang.Thread.sleep;
import java.net.*;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Sensor implements Runnable {

    private String ip;
    private String pressaoSangue;
    private int sistolica, diastolica;
    private Random pressao;
    private char movimento;
    private final int id;
    private int porta, portaBorda;
    private int batimentosCard;
    private PrintStream mensagem;
    private final Point ponto;
    private Scanner entrada;
    private Socket clientSocket;
    private String ipBorda;

    /**
     *
     * @param ip
     * @param porta
     * @throws IOException
     */
    public Sensor(String ip, int porta) throws IOException {
       
        this.ip = ip;
        this.porta = porta;
        this.movimento = 'R';
        pressao = new Random();
        sistolica = pressao.nextInt(20);
        diastolica = pressao.nextInt(15);
        pressaoSangue = sistolica + "/" + diastolica;
        Random localizaçãoPonto = new Random();
        ponto = new Point(localizaçãoPonto.nextInt(21), localizaçãoPonto.nextInt(21));
        System.out.println("Sensor Localizado em: " + "X" + ponto.getX() + " Y" + ponto.getY());
        Random r = new Random();
        this.id = r.nextInt(Integer.MAX_VALUE); // gerar ids aleatórios de 0 ao máximo que o pc aguenta
    }

    /**
     *
     * @return @throws UnknownHostException
     * @throws IOException
     */
    private PrintStream conectar() throws UnknownHostException, IOException {
        clientSocket = new Socket(ip, porta);
        System.out.println("Sensor Conectado com " + ip);
        PrintStream saida = new PrintStream(clientSocket.getOutputStream());
        saida.println("#S " + id + " " + ponto.getX() + " " + ponto.getY());
        System.out.println("Id Sensor " + id);
        entrada = new Scanner(clientSocket.getInputStream());
        return saida;
    }

    @Override
    public void run() {
        try {
            mensagem = this.conectar();

            while (true) {
                String mensagemRecebida = entrada.nextLine();
                this.tratarMensagemServidor(mensagemRecebida);
                Random batimentosRandom = new Random();
                ajustarDados(batimentosRandom.nextInt((201))); //gera valores aleatórios para o atributo batimentoCard
                Thread.sleep(10000);
            }
        } catch (InterruptedException e) {
        } catch (IOException ex) {
            Logger.getLogger(Sensor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Método para atribuição do movimento do sensor
     *
     * @param movimento
     */
    public void setMovimento(char movimento) {
        this.movimento = movimento;
    }

    /**
     * Método que retorna movimento do sensor
     *
     * @return char - Movimento do sensor
     */
    public char getMovimento() {
        return movimento;
    }

    /**
     * Método para retorno dos batimentos cardíacos do sensor
     *
     * @return batimentosCard
     */
    public int getBatimentosCard() {
        return batimentosCard;
    }

    /**
     * Método que atribue batimentos cardíacos ao sensor
     *
     * @param batimentosCard
     */
    public void setBatimentosCard(int batimentosCard) {
        this.batimentosCard = batimentosCard;
    }

    /**
     * Método que retorna pressão sanguínea do sensor
     *
     * @return pressaoSangue
     */
    public String getPressaoSangue() {
        return pressaoSangue;
    }

    /**
     * Método que atribue pressão sanguínea do sensor
     *
     * @param pressaoSangue
     */
    public void setPressaoSangue(String pressaoSangue) {
        this.pressaoSangue = pressaoSangue;
    }

    /**
     * Método para ajuste de batimentos caardíacos de acordo com o valor
     * inseridos no campo de texto da interface ou com o valor gerado
     * aleatoriamente
     *
     * @param batimentosGUI
     * @throws InterruptedException
     */
    public void ajustarDados(int batimentosGUI) throws InterruptedException {
        new Thread() {
            @Override
            public void run() {
                while (batimentosCard != batimentosGUI) {
                    if (batimentosCard >= batimentosGUI) {
                        batimentosCard = batimentosCard - 1; //batimentos cardíados são diminuídos até chegar ao valor inserido
                    } else if (batimentosCard <= batimentosGUI) {
                        batimentosCard = batimentosCard + 1; //batimentos cardíacos são aumentados até chegaar ao valor inserido
                    }
                    //enviando dados
                    String dados = movimento + "-" + batimentosCard + "-" + pressaoSangue;
                    dados = "#DADOS " + dados;
                    System.out.println(dados);
                    mensagem.println(dados); //envia dados do sensor ao servidor
                    try {
                        sleep(2000); //pausa o método por 2 segundos para que os dados não sejam enviados tão rapidamente
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Sensor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                batimentosCard = batimentosGUI;
            }
        }.start();
    }

    private void tratarMensagemServidor(String mensagemServidor) throws IOException {
        String vetor[] = mensagemServidor.split(" ");
        String codigo = vetor[0];
        System.out.println("Mensagem " + mensagemServidor);

        switch (codigo) {
            case "#DADOSBORDA":
                this.ipBorda = vetor[1];
                this.porta = Integer.parseInt(vetor[2]);
                clientSocket.close();
                System.out.println("Ip: " + ipBorda + " " + " Porta: " + porta);
                mensagem = this.conectar();
                break;
            case "#CONECTANUVEM":
                this.porta = Integer.parseInt(vetor[1]);
                clientSocket.close();
                System.out.println("Ip: " + ip + " " + " Porta: " + porta);
                mensagem = this.conectar();
                break;
        }
        

    }
}
