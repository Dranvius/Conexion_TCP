package org.vinni.servidor.gui;

import org.vinni.servidor.ServidorTcp;

import javax.swing.*;
import java.io.IOException;

/**
 * Author: Vinni
 */
public class PrincipalSrv extends javax.swing.JFrame {

    private static final int PORT = 12345;
    private ServidorTcp servidor;

    public PrincipalSrv() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        this.setTitle("Servidor ...");

        bIniciar = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        mensajesTxt = new JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);

        bIniciar.setFont(new java.awt.Font("Segoe UI", 0, 18));
        bIniciar.setText("INICIAR SERVIDOR");
        bIniciar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bIniciarActionPerformed(evt);
            }
        });

        getContentPane().add(bIniciar);
        bIniciar.setBounds(100, 90, 250, 40);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14));
        jLabel1.setForeground(new java.awt.Color(204, 0, 0));
        jLabel1.setText("SERVIDOR TCP : HOEL");
        getContentPane().add(jLabel1);
        jLabel1.setBounds(150, 10, 160, 17);

        mensajesTxt.setColumns(25);
        mensajesTxt.setRows(5);
        mensajesTxt.setEditable(false);
        jScrollPane1.setViewportView(mensajesTxt);
        getContentPane().add(jScrollPane1);
        jScrollPane1.setBounds(20, 160, 410, 70);

        setSize(new java.awt.Dimension(491, 290));
        setLocationRelativeTo(null);
    }
    // </editor-fold>

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PrincipalSrv().setVisible(true);
            }
        });
    }

    private void bIniciarActionPerformed(java.awt.event.ActionEvent evt) {
        iniciarServidor();
    }

    private void iniciarServidor() {
        if (servidor != null && servidor.isActivo()) {
            JOptionPane.showMessageDialog(this, "El servidor ya está iniciado");
            return;
        }

        try {
            servidor = new ServidorTcp(PORT, this::agregarMensaje);
            servidor.iniciar();
            bIniciar.setEnabled(false);
            JOptionPane.showMessageDialog(this, "Servidor iniciado en el puerto " + PORT);
        } catch (IOException ex) {
            agregarMensaje("No se pudo iniciar el servidor: " + ex.getMessage());
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo iniciar el servidor: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void agregarMensaje(String mensaje) {
        SwingUtilities.invokeLater(() -> mensajesTxt.append(mensaje + "\n"));
    }

    @Override
    public void dispose() {
        if (servidor != null) {
            servidor.close();
        }
        super.dispose();
    }

    private javax.swing.JButton bIniciar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextArea mensajesTxt;
    private javax.swing.JScrollPane jScrollPane1;
}
