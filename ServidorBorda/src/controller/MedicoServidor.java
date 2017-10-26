package controller;
/**Classe para objetos do tipo MedicoServidor contendo atributos e métodos para os mesmos.
 * @author Nadine Cerqueira Marques
 */

public class MedicoServidor {
    private String login, senha;
    private ClienteServidor clienteServidor;
    private boolean online = false;
    /**
     * Método que informa se o médico atual está online
     * @return online - Status do Médico 
     */
    public boolean isOnline() {
        return online;
    }
    /**
     * Método que seta o status do médico
     * @param online 
     */
    public void setOnline(boolean online) {
        this.online = online;
    }
    /**
     * Construtor
     * @param clienteServidor 
     */
    public MedicoServidor(ClienteServidor clienteServidor) {
        this.clienteServidor = clienteServidor;
        this.online = true;
    }
    /**
     * Método que retorna login do médico
     * @return 
     */
    public String getLogin() {
        return login;
    }
    /**
     * Método que atribue login ao médico
     * @param login 
     */
    public void setLogin(String login) {
        this.login = login;
    }
    /**
     * Método que retorna senha do médico
     * @return 
     */
    public String getSenha() {
        return senha;
    }
    /**
     * Método que seta senha ao atributo senha do médico
     * @param senha 
     */
    public void setSenha(String senha) {
        this.senha = senha;
    }
    /**
     * Método que retorna thread ClienteServidor do médico
     * @return 
     */
    public ClienteServidor getClienteServidor() {
        return clienteServidor;
    }
    /**
     * Método que seta thread ClienteServidor ao médico
     * @param clienteServidor 
     */
    public void setClienteServidor(ClienteServidor clienteServidor){
        this.clienteServidor = clienteServidor;
    }
}
    
