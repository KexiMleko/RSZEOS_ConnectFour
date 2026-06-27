package server;

public class GameMatch {

    private static final int ROWS = 6;
    private static final int COLS = 7;

    final String player1;
    final String player2;
    private String currentTurn;

    private final int[][] board = new int[ROWS][COLS];

    GameMatch(String player1, String player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.currentTurn = player1;
    }

    public boolean isPlayerTurn(String username) {
        return username.equals(currentTurn);
    }

    public String getOpponent(String username) {
        return username.equals(player1) ? player2 : player1;
    }

    public int dropDisc(int col) {
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][col] == 0) {
                board[row][col] = currentTurn.equals(player1) ? 1 : 2;
                return row;
            }
        }
        return -1;
    }

    public boolean checkWin() {
        int p = currentTurn.equals(player1) ? 1 : 2;

        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c <= COLS - 4; c++)
                if (board[r][c] == p && board[r][c+1] == p && board[r][c+2] == p && board[r][c+3] == p)
                    return true;

        for (int r = 0; r <= ROWS - 4; r++)
            for (int c = 0; c < COLS; c++)
                if (board[r][c] == p && board[r+1][c] == p && board[r+2][c] == p && board[r+3][c] == p)
                    return true;

        for (int r = 0; r <= ROWS - 4; r++)
            for (int c = 0; c <= COLS - 4; c++)
                if (board[r][c] == p && board[r+1][c+1] == p && board[r+2][c+2] == p && board[r+3][c+3] == p)
                    return true;

        for (int r = 0; r <= ROWS - 4; r++)
            for (int c = 3; c < COLS; c++)
                if (board[r][c] == p && board[r+1][c-1] == p && board[r+2][c-2] == p && board[r+3][c-3] == p)
                    return true;

        return false;
    }

    public boolean isBoardFull() {
        for (int c = 0; c < COLS; c++)
            if (board[0][c] == 0) return false;
        return true;
    }

    public void switchTurn() {
        currentTurn = currentTurn.equals(player1) ? player2 : player1;
    }

    public String getCurrentTurn() {
        return currentTurn;
    }
}
