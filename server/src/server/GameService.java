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
    }

    public void logout(String username, ConnectedClient self) {
        sessionRegistry.removeIfSame(username, self);
    }

    public void handleInvite(String from, String target) {
        sessionRegistry.get(target).send(MessageParser.build(MessageType.INVITE_NOTIFICATION));
    }

    public void handleInviteResponse(String from, String inviter, boolean accepted) {
        if (accepted) {
            startMatch(inviter, from);
        }
        sessionRegistry.get(inviter).send(MessageParser.build(MessageType.INVITE_RESULT, Boolean.toString(accepted)));
    }

    public void handleMove(String from, int col) {
        GameMatch match = matches.get(from);
        match.dropDisc(col);
        boolean hasWon = match.checkWin();
        if (hasWon) {
            endMatch(match.player1, match.player2);
        }
    }

    public void handlePlayAgain(String from, boolean want) {
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
