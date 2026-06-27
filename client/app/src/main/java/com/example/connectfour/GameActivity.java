package com.example.connectfour;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.connectfour.net.MessageProtocol;
import com.example.connectfour.net.NetworkClient;

public class GameActivity extends AppCompatActivity {

    public static final String EXTRA_USERNAME = "username";
    public static final String EXTRA_OPPONENT = "opponent";
    public static final String EXTRA_PLAYS_FIRST = "playsFirst";

    private static final int ROWS = 6;
    private static final int COLS = 7;
    private static final int EMPTY = 0;
    private static final int RED = 1;
    private static final int BLUE = 2;

    private final ImageView[][] cells = new ImageView[ROWS][COLS];
    private final int[][] board = new int[ROWS][COLS];

    private final NetworkClient net = NetworkClient.getInstance();

    private GridLayout boardGrid;
    private TextView tvTurn;
    private TextView tvStatus;

    private String opponent;
    private boolean playsFirst;
    private int myDisc;
    private boolean myTurn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        opponent = getIntent().getStringExtra(EXTRA_OPPONENT);
        playsFirst = getIntent().getBooleanExtra(EXTRA_PLAYS_FIRST, false);
        myDisc = playsFirst ? RED : BLUE;
        myTurn = playsFirst;

        boardGrid = findViewById(R.id.boardGrid);
        tvTurn = findViewById(R.id.tvTurn);
        tvStatus = findViewById(R.id.tvStatus);

        buildBoard();
        resetBoard();

        String myColor = getString(playsFirst ? R.string.color_red : R.string.color_blue);
        tvTurn.setText(getString(R.string.game_header, myColor, opponent));
        updateStatus();
    }

    private void onColumnTapped(int col) {
        if (!myTurn) {
            Toast.makeText(this, R.string.warn_not_your_turn, Toast.LENGTH_SHORT).show();
            return;
        }

        int row = dropRow(col);
        if (row == -1) {
            Toast.makeText(this, R.string.warn_column_full, Toast.LENGTH_SHORT).show();
            return;
        }

        placeDisc(row, col, myDisc);
        net.send(MessageProtocol.build(MessageProtocol.MOVE_REQUEST, String.valueOf(col)));

        myTurn = false;
        updateStatus();
    }

    private void placeDisc(int row, int col, int disc) {
        board[row][col] = disc;
        cells[row][col].setImageResource(disc == RED ? R.drawable.disc_red : R.drawable.disc_blue);
    }

    private int dropRow(int col) {
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][col] == EMPTY) {
                return row;
            }
        }
        return -1;
    }

    private void updateStatus() {
        if (myTurn) {
            tvStatus.setText(R.string.status_your_turn);
        } else {
            tvStatus.setText(getString(R.string.status_their_turn, opponent));
        }
    }

    private void buildBoard() {
        boardGrid.setColumnCount(COLS);
        boardGrid.setRowCount(ROWS);

        int cellSize = cellSize();
        int margin = dp(4);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                final int column = col;
                ImageView cell = new ImageView(this);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(row), GridLayout.spec(col));
                params.width = cellSize;
                params.height = cellSize;
                params.setMargins(margin, margin, margin, margin);
                cell.setLayoutParams(params);
                cell.setOnClickListener(v -> onColumnTapped(column));

                cells[row][col] = cell;
                boardGrid.addView(cell);
            }
        }
    }

    private void resetBoard() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                board[row][col] = EMPTY;
                cells[row][col].setImageResource(R.drawable.disc_empty);
            }
        }
    }

    private int cellSize() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int available = metrics.widthPixels - dp(16) * 2 - dp(6) * 2;
        return available / COLS - dp(4) * 2;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
