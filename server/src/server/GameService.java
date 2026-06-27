package server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameService {
    private final SessionRegistry sessionRegistry = new SessionRegistry();
    private final Map<String, GameMatch> matches = new ConcurrentHashMap<>();

    public void register(String username, ConnectedClient client) {
        if (sessionRegistry.contains(username)) {
            sessionRegistry.get(username).disconnect();
        }
        sessionRegistry.add(username, client);
        String msg = MessageParser.build(MessageType.LOGIN_RESPONSE);
        notify(client, msg);
    }

    public void logout(String username, ConnectedClient self) {
        sessionRegistry.removeIfSame(username, self);
    }

    public void handleInvite(String from, String target) {

        ConnectedClient client = sessionRegistry.get(target);
        String msg = MessageParser.build(MessageType.INVITE_NOTIFICATION, from);
        notify(client, msg);
    }

    public void handleInviteResponse(String from, String inviter, boolean accepted) {
        if (accepted) {
            startMatch(inviter, from);
        }
        ConnectedClient client = sessionRegistry.get(inviter);
        String msg = MessageParser.build(MessageType.INVITE_RESULT, Boolean.toString(accepted));
        notify(client, msg);
    }

    public void handleMove(String from, int col) {
        GameMatch match = matches.get(from);
        match.dropDisc(col);
        boolean hasWon = match.checkWin();
        String msg = MessageParser.build(MessageType.MOVE_UPDATE, from);
        if (hasWon) {
            endMatch(match.player1, match.player2);
            msg = MessageParser.build(MessageType.GAME_OVER, from);
        }
        notify(sessionRegistry.get(match.player1), msg);
        notify(sessionRegistry.get(match.player2), msg);
    }

    public void handlePlayAgainResponse(String from, String inviter, boolean accepted) {

        if (accepted) {
            startMatch(inviter, from);
        }
        ConnectedClient client = sessionRegistry.get(inviter);
        String msg = MessageParser.build(MessageType.INVITE_RESULT, Boolean.toString(accepted));
        notify(client, msg);
    }

    public void handlePlayAgain(String from, String prev_player1, String prev_player2) {

    }

    private void startMatch(String player1, String player2) {
        GameMatch match = new GameMatch(player1, player2);
        matches.put(player1, match);
        matches.put(player2, match);
    }

    private void endMatch(String player1, String player2) {
        matches.remove(player1);
        matches.remove(player2);
    }

    private void notify(ConnectedClient client, String msg) {
        if (client == null)
            return;
        client.send(msg);
    }
}
