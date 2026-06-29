package com.example.connectfour;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.connectfour.net.MessageProtocol;
import com.example.connectfour.net.NetworkClient;

import java.util.ArrayList;
import java.util.List;

public class LobbyActivity extends AppCompatActivity implements NetworkClient.Listener {

    public static final String EXTRA_USERNAME = "username";

    private ListView lvPlayers;
    private TextView tvStatus;

    private ArrayAdapter<String> adapter;
    private final List<String> players = new ArrayList<>();

    private final NetworkClient net = NetworkClient.getInstance();
    private String username;
    private String pendingInvitee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lobby);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.lobby), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        username = getIntent().getStringExtra(EXTRA_USERNAME);

        lvPlayers = findViewById(R.id.lvPlayers);
        tvStatus = findViewById(R.id.tvStatus);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, players);
        lvPlayers.setAdapter(adapter);
        lvPlayers.setOnItemClickListener((parent, view, position, id) -> invite(players.get(position)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        net.setListener(this);
    }

    private void invite(String target) {
        pendingInvitee = target;
        net.send(MessageProtocol.build(MessageProtocol.INVITE_REQUEST, target));
        setStatus(getString(R.string.status_invite_sent, target));
    }

    @Override
    public void onConnected() {
    }

    @Override
    public void onMessage(String line) {
        runOnUiThread(() -> handle(line));
    }

    @Override
    public void onDisconnected() {
        runOnUiThread(() -> setStatus(getString(R.string.status_disconnected)));
    }

    @Override
    public void onError(Exception e) {
        runOnUiThread(() -> setStatus(getString(R.string.status_error, e.getMessage())));
    }

    private void handle(String line) {
        switch (MessageProtocol.getType(line)) {
            case MessageProtocol.PLAYERS_LIST:
                players.clear();
                players.addAll(parsePlayers(MessageProtocol.getPayload(line)));
                adapter.notifyDataSetChanged();
                break;
            case MessageProtocol.INVITE_NOTIFICATION:
                showInvite(MessageProtocol.getPayload(line));
                break;
            case MessageProtocol.INVITE_RESULT:
                if (Boolean.parseBoolean(MessageProtocol.getPayload(line))) {
                    startGame(pendingInvitee, true);
                } else {
                    setStatus(getString(R.string.status_invite_declined));
                }
                break;
            default:
                break;
        }
    }

    private void showInvite(final String from) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.invite_title)
                .setMessage(getString(R.string.invite_message, from))
                .setCancelable(false)
                .setPositiveButton(R.string.accept, (dialog, which) -> {
                    net.send(MessageProtocol.build(MessageProtocol.INVITE_RESPONSE, from, "true"));
                    startGame(from, false);
                })
                .setNegativeButton(R.string.decline, (dialog, which) ->
                        net.send(MessageProtocol.build(MessageProtocol.INVITE_RESPONSE, from, "false")))
                .show();
    }

    private void startGame(String opponent, boolean playsFirst) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_USERNAME, username);
        intent.putExtra(GameActivity.EXTRA_OPPONENT, opponent);
        intent.putExtra(GameActivity.EXTRA_PLAYS_FIRST, playsFirst);
        startActivity(intent);
    }

    private List<String> parsePlayers(String payload) {
        String s = payload.trim();
        if (s.startsWith("[")) {
            s = s.substring(1);
        }
        if (s.endsWith("]")) {
            s = s.substring(0, s.length() - 1);
        }
        List<String> result = new ArrayList<>();
        for (String part : s.split(",")) {
            String name = part.trim();
            if (!name.isEmpty() && !name.equals(username)) {
                result.add(name);
            }
        }
        return result;
    }

    private void setStatus(String text) {
        tvStatus.setText(text);
    }
}
