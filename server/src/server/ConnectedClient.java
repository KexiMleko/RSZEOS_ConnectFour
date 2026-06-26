package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectedClient implements Runnable {
    private final Socket socket;
    private final GameService gameService;
    private String username;
    private PrintWriter out;

    public ConnectedClient(Socket socket, GameService gameService) {
        this.socket = socket;
        this.gameService = gameService;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String first = in.readLine();
            if (first != null && first.startsWith("LOGIN:")) {
                username = first.substring(6).trim();
                gameService.register(username, this);
            }

            String line;
            while ((line = in.readLine()) != null) {
                handle(line.trim());
            }
        } catch (IOException e) {
            // client disconnected
        } finally {
            if (username != null) {
                gameService.logout(username, this);
            }
            closeSocket();
        }
    }

    private void handle(String msg) {
        if (msg.startsWith("INVITE:")) {
            String target = msg.substring(7);
            gameService.handleInvite(username, target);
        } else if (msg.startsWith("INVITE_RESPONSE:")) {
            // INVITE_RESPONSE:inviterUsername:true|false
            String[] parts = msg.split(":", 3);
            if (parts.length == 3) {
                boolean accepted = Boolean.parseBoolean(parts[2]);
                gameService.handleInviteResponse(username, parts[1], accepted);
            }
        } else if (msg.startsWith("MOVE:")) {
            int col = Integer.parseInt(msg.substring(5));
            gameService.handleMove(username, col);
        } else if (msg.startsWith("PLAY_AGAIN:")) {
            boolean want = Boolean.parseBoolean(msg.substring(11));
            gameService.handlePlayAgain(username, want);
        }
    }

    public void send(String msg) {
        synchronized (out) {
            out.println(msg);
        }
    }

    public void disconnect() {
        closeSocket();
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
