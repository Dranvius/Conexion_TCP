package org.vinni.servidor;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Prueba de integración del flujo multi-cliente.
 */
public class ServidorTcpTest extends TestCase {

    public void testServidorConfirmaYRetransmiteATodosLosClientes() throws Exception {
        int port = buscarPuertoLibre();

        try (ServidorTcp servidor = new ServidorTcp(port, mensaje -> { })) {
            servidor.iniciar();

            try (Socket socket1 = new Socket("localhost", port);
                 Socket socket2 = new Socket("localhost", port)) {

                socket1.setSoTimeout(3000);
                socket2.setSoTimeout(3000);

                PrintWriter out1 = writer(socket1);
                BufferedReader in1 = reader(socket1);
                PrintWriter out2 = writer(socket2);
                BufferedReader in2 = reader(socket2);

                out1.println("CLIENTE 1");
                out2.println("CLIENTE 2");

                assertEquals("[SERVIDOR] Conexión confirmada para CLIENTE 1", in1.readLine());
                assertEquals("[SERVIDOR] Conexión confirmada para CLIENTE 2", in2.readLine());

                out1.println("Hola desde cliente 1");

                assertEquals("[SERVIDOR] Mensaje recibido: Hola desde cliente 1", in1.readLine());
                assertEquals("[CLIENTE 1] Hola desde cliente 1", in1.readLine());
                assertEquals("[CLIENTE 1] Hola desde cliente 1", in2.readLine());
            }
        }
    }

    private static int buscarPuertoLibre() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static PrintWriter writer(Socket socket) throws Exception {
        return new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
    }

    private static BufferedReader reader(Socket socket) throws Exception {
        return new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    }
}
