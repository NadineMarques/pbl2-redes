package model;

/**Classe para objetos do tipo ClienteSensor contendo atributos e métodos para os mesmos.
 * @author Nadine Cerqueira Marques
 */

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClienteSensor implements Runnable {

    private final String ip;
    private String pressaoSangue;
    private char movimento;
    private final int id;
    private final int porta;
    private int batimentosCard;
    private PrintStream mensagem;
    
    /**
     * 
     * @param ip
     * @param porta
     * @throws IOException 
     */
    public ClienteSensor(String ip, int porta) throws IOException {
        this.ip = ip;
        this.porta = porta;
        this.movimento = 'R';
        pressaoSangue = "0/0";
        Random r = new Random();
        this.id = r.nextInt(Integer.MAX_VALUE); // gerar ids aleatórios de 0 ao máximo que o pc aguenta
    }
    
    /**
     * 
     * @return
     * @throws UnknownHostException
     * @throws IOException 
     */
    private PrintStream conectar() throws UnknownHostException, IOException {
        Socket clientSocket = new Socket(ip, porta);
        System.out.println("Sensor Conectado");
        PrintStream saida = new PrintStream(clientSocket.getOutputStream());
        saida.println("#S " + id);
        System.out.println("Id Sensor " + id);
        return saida;
    }
    
    @Override
    public void run() {
        try {
            mensagem = this.conectar();

            while (true) {
                Random batimentosRandom = new Random();
                ajustarDados(batimentosRandom.nextInt((201))); //gera valores aleatórios para o atributo batimentoCard
                Thread.sleep(10000);
            }
        } catch (InterruptedException e) {
        } catch (IOException ex) {
            Logger.getLogger(ClienteSensor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Método para atribuição do movimento do sensor
     * @param movimento 
     */
    public void setMovimento(char movimento) {
        this.movimento = movimento;
    }
    /**
     * Método que retorna movimento do sensor
     * @return char - Movimento do sensor
     */
    public char getMovimento(){
        return movimento;
    }
    
    /**
     * Método para retorno dos batimentos cardíacos do sensor
     * @return batimentosCard
     */
    public int getBatimentosCard() {
        return batimentosCard;
    }
    /**
     * Método que atribue batimentos cardíacos ao sensor
     * @param batimentosCard 
     */
    public void setBatimentosCard(int batimentosCard) {
        this.batimentosCard = batimentosCard;
    }
    /**
     * Método que retorna pressão sanguínea do sensor
     * @return pressaoSangue 
     */
    public String getPressaoSangue() {
        return pressaoSangue;
    }
    /**
     * Método que atribue pressão sanguínea do sensor
     * @param pressaoSangue 
     */
    public void setPressaoSangue(String pressaoSangue) {
        this.pressaoSangue = pressaoSangue;
    }
    /**
     * Método para ajuste de batimentos caardíacos de acordo com o valor inseridos no campo de texto da interface ou com o valor gerado aleatoriamente 
     * @param batimentosGUI
     * @throws InterruptedException 
     */
    public void ajustarDados(int batimentosGUI) throws InterruptedException {
        new Thread() {
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
                    mensagem.println(dados); //envia dados do sensor ao servidor
                    try {
                        sleep(2000); //pausa o método por 2 segundos para que os dados não sejam enviados tão rapidamente
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ClienteSensor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                batimentosCard = batimentosGUI;
            }
        }.start();

    }
}
