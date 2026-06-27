package server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UserRegistry {
    private final Set<String> availableUsers = ConcurrentHashMap.newKeySet();

    public void addUser(String username) {
        availableUsers.add(username);
    }

    public boolean contains(String username) {
        return availableUsers.contains(username);
    }

    public Collection<String> all() {
        return new ArrayList<>(availableUsers);
    }

    public void removeUser(String username) {
        availableUsers.remove(username);
    }
}
