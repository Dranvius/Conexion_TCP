package org.vinni.servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Servidor TCP multi-cliente.
 *
 * Cada cliente envía su nombre como primera línea después de conectarse.
 * A partir de ese momento, cada mensaje recibido se confirma al remitente y
 * se retransmite a todos los clientes conectados, incluido el remitente.
 */
public class ServidorTcp implements AutoCloseable {

    private final int port;
    private final Consumer<String> logger;
    private final List<ClienteConectado> clientes = new CopyOnWriteArrayList<>();

    private volatile boolean activo;
    private ServerSocket serverSocket;
    private Thread hiloAceptacion;

    public ServidorTcp(int port, Consumer<String> logger) {
        this.port = port;
        this.logger = logger != null ? logger : mensaje -> { };
    }

    public synchronized void iniciar() throws IOException {
        if (activo) {
            return;
        }

        serverSocket = new ServerSocket(port);
        activo = true;
        log("Servidor TCP listo en puerto " + serverSocket.getLocalPort());

        hiloAceptacion = new Thread(this::aceptarClientes, "tcp-servidor-aceptacion");
        hiloAceptacion.setDaemon(true);
        hiloAceptacion.start();
    }

    private void aceptarClientes() {
        while (activo) {
            try {
                Socket socket = serverSocket.accept();
                log("Nuevo cliente conectado desde " + socket.getRemoteSocketAddress());

                Thread hiloCliente = new Thread(
                        () -> atenderCliente(socket),
                        "tcp-cliente-" + socket.getPort()
                );
                hiloCliente.setDaemon(true);
                hiloCliente.start();
            } catch (IOException e) {
                if (activo) {
                    log("Error aceptando cliente: " + e.getMessage());
                }
            }
        }
    }

    private void atenderCliente(Socket socket) {
        ClienteConectado cliente = null;

        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
            );
            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(),
                    true,
                    StandardCharsets.UTF_8
            );

            String nombreCliente = normalizarNombre(in.readLine(), socket);
            cliente = new ClienteConectado(nombreCliente, socket, out);
            clientes.add(cliente);

            out.println("[SERVIDOR] Conexión confirmada para " + nombreCliente);
            log(nombreCliente + " identificado. Clientes conectados: " + clientes.size());

            String linea;
            while ((linea = in.readLine()) != null) {
                if (linea.isBlank()) {
                    continue;
                }

                log("Mensaje recibido de " + nombreCliente + ": " + linea);
                out.println("[SERVIDOR] Mensaje recibido: " + linea);
                broadcast("[" + nombreCliente + "] " + linea);
            }
        } catch (IOException e) {
            if (activo) {
                String nombre = cliente != null ? cliente.nombre() : "cliente";
                log("Conexión cerrada con " + nombre + ": " + e.getMessage());
            }
        } finally {
            if (cliente != null) {
                clientes.remove(cliente);
                cliente.cerrar();
                log(cliente.nombre() + " desconectado. Clientes conectados: " + clientes.size());
            } else {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private String normalizarNombre(String nombre, Socket socket) {
        if (nombre == null || nombre.isBlank()) {
            return "CLIENTE-" + socket.getPort();
        }

        String limpio = nombre.trim();
        return limpio.length() <= 40 ? limpio : limpio.substring(0, 40);
    }

    public void broadcast(String mensaje) {
        for (ClienteConectado cliente : clientes) {
            cliente.enviar(mensaje);
        }
    }

    public int getClientesConectados() {
        return clientes.size();
    }

    public boolean isActivo() {
        return activo;
    }

    private void log(String mensaje) {
        logger.accept(mensaje);
    }

    @Override
    public synchronized void close() {
        activo = false;

        for (ClienteConectado cliente : clientes) {
            cliente.cerrar();
        }
        clientes.clear();

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private record ClienteConectado(String nombre, Socket socket, PrintWriter out) {
        private void enviar(String mensaje) {
            out.println(mensaje);
        }

        private void cerrar() {
            out.close();
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
