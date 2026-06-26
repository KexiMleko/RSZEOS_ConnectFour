package server;

public class GameService {
    private final SessionRegistry sessionRegistry = new SessionRegistry();

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
    }

    public void handleInviteResponse(String from, String inviter, boolean accepted) {
    }

    public void handleMove(String from, int col) {
    }

    public void handlePlayAgain(String from, boolean want) {
    }

    private void notify(ConnectedClient client, String msg) {
        if (client == null) return;
        client.send(msg);
    }
}
