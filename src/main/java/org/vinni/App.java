package org.vinni;

import org.vinni.cliente.gui.PrincipalCli;
import org.vinni.servidor.gui.PrincipalSrv;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {

        System.out.println( "Iniciando Aplicacion" );

        java.awt.EventQueue.invokeLater(() -> {
            new PrincipalSrv().setVisible(true);
        });

        java.awt.EventQueue.invokeLater(() -> {
            new PrincipalCli("CLIENTE 1").setVisible(true);
        });

        java.awt.EventQueue.invokeLater(() -> {
            new PrincipalCli("CLIENTE 2").setVisible(true);
        });
    }
}
