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
            if (first != null && MessageParser.getType(first) == MessageType.LOGIN_REQUEST) {
                username = MessageParser.getPayload(first).trim();
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
        MessageType type = MessageParser.getType(msg);
        String[] args = MessageParser.getArgs(msg);

        switch (type) {
            case INVITE_REQUEST:
                gameService.handleInvite(username, args[0]);
                break;
            case INVITE_RESPONSE:
                gameService.handleInviteResponse(username, args[0], Boolean.parseBoolean(args[1]));
                break;
            case MOVE_REQUEST:
                gameService.handleMove(username, Integer.parseInt(args[0]));
                break;
            case PLAY_AGAIN_REQUEST:
                gameService.handlePlayAgain(username, Boolean.parseBoolean(args[0]));
                break;
            default:
                break;
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
