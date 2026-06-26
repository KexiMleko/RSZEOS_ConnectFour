package server;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionRegistry {
	private final Map<String, ConnectedClient> sessions = new ConcurrentHashMap<>();

	public void add(String username, ConnectedClient client) {
		sessions.put(username, client);
	}

	public void remove(String username) {
		sessions.remove(username);
	}

	public void removeIfSame(String username, ConnectedClient client) {
		sessions.remove(username, client);
	}

	public ConnectedClient get(String username) {
		return sessions.get(username);
	}

	public boolean contains(String username) {
		return sessions.containsKey(username);
	}

	public Collection<ConnectedClient> all() {
		return sessions.values();
	}
}