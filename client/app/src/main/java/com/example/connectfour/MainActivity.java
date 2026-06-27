package com.example.connectfour;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity implements NetworkClient.Listener {

    private EditText etIp;
    private EditText etPort;
    private EditText etUsername;
    private Button btnConnect;
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
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etIp = findViewById(R.id.etIp);
        etPort = findViewById(R.id.etPort);
        etUsername = findViewById(R.id.etUsername);
        btnConnect = findViewById(R.id.btnConnect);
        lvPlayers = findViewById(R.id.lvPlayers);
        tvStatus = findViewById(R.id.tvStatus);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, players);
        lvPlayers.setAdapter(adapter);
        lvPlayers.setOnItemClickListener((parent, view, position, id) -> invite(players.get(position)));

        btnConnect.setOnClickListener(v -> connect());
    }

    @Override
    protected void onResume() {
        super.onResume();
        net.setListener(this);
    }

    private void connect() {
        String ip = etIp.getText().toString().trim();
        String portText = etPort.getText().toString().trim();
        username = etUsername.getText().toString().trim();

        if (ip.isEmpty() || portText.isEmpty() || username.isEmpty()) {
            toast(getString(R.string.error_missing_fields));
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            toast(getString(R.string.error_invalid_port));
            return;
        }

        setStatus(getString(R.string.status_connecting, ip, port));
        net.setListener(this);
        net.connect(ip, port);
    }

    private void invite(String target) {
        pendingInvitee = target;
        net.send(MessageProtocol.build(MessageProtocol.INVITE_REQUEST, target));
        setStatus(getString(R.string.status_invite_sent, target));
    }

    @Override
    public void onConnected() {
        net.send(MessageProtocol.build(MessageProtocol.LOGIN_REQUEST, username));
        runOnUiThread(() -> setStatus(getString(R.string.status_registering, username)));
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
        String type = MessageProtocol.getType(line);
        switch (type) {
            case MessageProtocol.LOGIN_RESPONSE:
                setStatus(getString(R.string.status_registered, username));
                break;
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

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
