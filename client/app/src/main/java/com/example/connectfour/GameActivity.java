package com.example.connectfour;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GameActivity extends AppCompatActivity {

    public static final String EXTRA_USERNAME = "username";
    public static final String EXTRA_OPPONENT = "opponent";
    public static final String EXTRA_PLAYS_FIRST = "playsFirst";

    private static final int ROWS = 6;
    private static final int COLS = 7;

    private final ImageView[][] cells = new ImageView[ROWS][COLS];

    private GridLayout boardGrid;
    private TextView tvTurn;

    private String opponent;
    private boolean playsFirst;

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

        boardGrid = findViewById(R.id.boardGrid);
        tvTurn = findViewById(R.id.tvTurn);

        buildBoard();
        resetBoard();

        String myColor = getString(playsFirst ? R.string.color_red : R.string.color_blue);
        tvTurn.setText(getString(R.string.game_header, myColor, opponent));
    }

    private void buildBoard() {
        boardGrid.setColumnCount(COLS);
        boardGrid.setRowCount(ROWS);

        int cellSize = cellSize();
        int margin = dp(4);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                ImageView cell = new ImageView(this);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(row), GridLayout.spec(col));
                params.width = cellSize;
                params.height = cellSize;
                params.setMargins(margin, margin, margin, margin);
                cell.setLayoutParams(params);

                cells[row][col] = cell;
                boardGrid.addView(cell);
            }
        }
    }

    private void resetBoard() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
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
