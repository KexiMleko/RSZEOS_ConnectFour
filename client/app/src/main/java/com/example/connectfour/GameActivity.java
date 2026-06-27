package com.example.connectfour;

import android.os.Bundle;
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

        String opponent = getIntent().getStringExtra(EXTRA_OPPONENT);
        boolean playsFirst = getIntent().getBooleanExtra(EXTRA_PLAYS_FIRST, false);

        TextView info = findViewById(R.id.tvGameInfo);
        info.setText(getString(R.string.game_info, opponent, playsFirst ? 1 : 2));
    }
}
