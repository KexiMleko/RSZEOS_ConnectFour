package server;

public enum MessageType {

    // incoming 
    LOGIN_REQUEST,
    INVITE_REQUEST,
    INVITE_RESPONSE,
    MOVE_REQUEST,
    PLAY_AGAIN_REQUEST,

    // outgoing 
    LOGIN_RESPONSE,
    PLAYERS_LIST,
    INVITE_NOTIFICATION,
    INVITE_RESULT,
    MOVE_UPDATE,
    GAME_OVER,
    PLAY_AGAIN_RESPONSE,

    UNKNOWN
}
