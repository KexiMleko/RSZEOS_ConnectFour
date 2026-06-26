package server;

public class MessageParser {

    public static MessageType getType(String msg) {
        int idx = msg.indexOf(':');
        String raw = idx == -1 ? msg : msg.substring(0, idx);
        try {
            return MessageType.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return MessageType.UNKNOWN;
        }
    }

    public static String getPayload(String msg) {
        int idx = msg.indexOf(':');
        return idx == -1 ? "" : msg.substring(idx + 1);
    }

    public static String[] getArgs(String msg) {
        return getPayload(msg).split(":", -1);
    }

    public static String build(String type, String... args) {
        if (args.length == 0) return type;
        return type + ":" + String.join(":", args);
    }
}
