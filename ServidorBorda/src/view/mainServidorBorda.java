/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.io.IOException;
import model.ServidorBorda;

/**
 *
 * @author nady_
 */
public class mainServidorBorda {
    public static void main(String[] args) throws IOException {
        ServidorBorda servidor = new ServidorBorda();
        new Thread(servidor).start(); //inicia a thread
    }
}
