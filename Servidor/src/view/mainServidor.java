/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.io.IOException;
import model.ServidorNuvem;

/**
 *
 * @author nady_
 */
public class mainServidor {
    public static void main(String[] args) throws IOException {
        ServidorNuvem s = new ServidorNuvem();
        s.iniciarServidor();
    }
}
