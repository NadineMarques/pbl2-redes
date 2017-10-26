package controller;
/**Classe para objetos do tipo SensorServidor contendo atributos e métodos para os mesmos.
 * @author Nadine Cerqueira Marques
 */

public class SensorServidor {

    private char movimento;
    private int batimentosCard;
    private String pressaoSangue;
    private ClienteServidor clienteSensor;
    
    /**
     * Construtor
     * @param clienteSensor 
     */
    public SensorServidor(ClienteServidor clienteSensor) {
        this.clienteSensor = clienteSensor;
    }
    /**
     * Método que retorna movimento do paciente
     * @return char - Movimento do Paciente
     */
    public char getMovimento() {
        return movimento;
    }
    
    /**
     * Método para atribuição do movimento do paciente
     * @param movimento 
     */
    public void setMovimento(char movimento) {
        this.movimento = movimento;
    }
    /**
     * Método que retorna thread ClienteServidor do sensor
     * @return 
     */
    public ClienteServidor getClienteSensor() {
        return clienteSensor;
    }
    /**
     * Método para retorno dos batimentos cardíacos do paciente
     * @return batimentosCard
     */
    public int getBatimentosCard() {
        return batimentosCard;
    }
    /**
     * Método para setar os batimentos cardíacos do paciente
     * @param batimentosCard
     */
    public void setBatimentosCard(int batimentosCard) {
        this.batimentosCard = batimentosCard;
    }
    /**
     * Método para retornar pressão sanguínea do paciente
     * @return 
     */
    public String getPressaoSangue() {
        return pressaoSangue;
    }
    /**
     * Método que seta pressão sanguínea do paciente
     * @param pressaoSangue 
     */
    public void setPressaoSangue(String pressaoSangue) {
        this.pressaoSangue = pressaoSangue;
    }
    
    /**
     * Método para retornar id do sensor
     * @return 
     */
    @Override
    public String toString(){
        return "" + (clienteSensor.getId());
    }
}
