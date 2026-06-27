package com.example.connectfour.net;

public final class MessageProtocol {

    private MessageProtocol() {
    }

    // server -> client
    public static final String LOGIN_RESPONSE = "LOGIN_RESPONSE";
    public static final String PLAYERS_LIST = "PLAYERS_LIST";
    public static final String INVITE_NOTIFICATION = "INVITE_NOTIFICATION";
    public static final String INVITE_RESULT = "INVITE_RESULT";
    public static final String MOVE_UPDATE = "MOVE_UPDATE";
    public static final String GAME_OVER = "GAME_OVER";
    public static final String PLAY_AGAIN_RESPONSE = "PLAY_AGAIN_RESPONSE";

    // client -> server
    public static final String LOGIN_REQUEST = "LOGIN_REQUEST";
    public static final String INVITE_REQUEST = "INVITE_REQUEST";
    public static final String INVITE_RESPONSE = "INVITE_RESPONSE";
    public static final String MOVE_REQUEST = "MOVE_REQUEST";
    public static final String PLAY_AGAIN_REQUEST = "PLAY_AGAIN_REQUEST";

    public static String getType(String msg) {
        int i = msg.indexOf(':');
        return i == -1 ? msg : msg.substring(0, i);
    }

    public static String getPayload(String msg) {
        int i = msg.indexOf(':');
        return i == -1 ? "" : msg.substring(i + 1);
    }

    public static String[] getArgs(String msg) {
        return getPayload(msg).split(":", -1);
    }

    public static String build(String type, String... args) {
        if (args.length == 0) {
            return type;
        }
        return type + ":" + String.join(":", args);
    }
}
