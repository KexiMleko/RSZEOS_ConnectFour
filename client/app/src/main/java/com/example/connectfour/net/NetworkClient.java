package com.example.connectfour.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkClient {

    public interface Listener {
        void onConnected();

        void onMessage(String line);

        void onDisconnected();

        void onError(Exception e);
    }

    private static final NetworkClient INSTANCE = new NetworkClient();
    private static final int CONNECT_TIMEOUT_MS = 8000;

    private Socket socket;
    private PrintWriter out;
    private ExecutorService sender;
    private volatile Listener listener;
    private volatile boolean running;

    private NetworkClient() {
    }

    public static NetworkClient getInstance() {
        return INSTANCE;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public boolean isConnected() {
        return running && socket != null && !socket.isClosed();
    }

    public void connect(final String host, final int port) {
        new Thread(() -> {
            try {
                Socket s = new Socket();
                s.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
                socket = s;
                out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true);
                sender = Executors.newSingleThreadExecutor();
                running = true;
                emitConnected();
                readLoop();
            } catch (Exception e) {
                emitError(e);
            }
        }, "net-connect").start();
    }

    private void readLoop() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while (running && (line = in.readLine()) != null) {
                emitMessage(line.trim());
            }
        } catch (IOException e) {
            if (running) {
                emitError(e);
            }
        } finally {
            emitDisconnected();
        }
    }

    public void send(final String msg) {
        final ExecutorService ex = sender;
        if (ex == null) {
            return;
        }
        ex.execute(() -> {
            PrintWriter w = out;
            if (w != null) {
                w.println(msg);
            }
        });
    }

    public void disconnect() {
        running = false;
        if (sender != null) {
            sender.shutdownNow();
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
        socket = null;
        out = null;
    }

    private void emitConnected() {
        Listener l = listener;
        if (l != null) {
            l.onConnected();
        }
    }

    private void emitMessage(String msg) {
        Listener l = listener;
        if (l != null) {
            l.onMessage(msg);
        }
    }

    private void emitDisconnected() {
        Listener l = listener;
        if (l != null) {
            l.onDisconnected();
        }
    }

    private void emitError(Exception e) {
        Listener l = listener;
        if (l != null) {
            l.onError(e);
        }
    }
}
