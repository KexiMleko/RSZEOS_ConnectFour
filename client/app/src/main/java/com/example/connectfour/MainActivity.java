package com.example.connectfour;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.connectfour.net.MessageProtocol;
import com.example.connectfour.net.NetworkClient;

public class MainActivity extends AppCompatActivity implements NetworkClient.Listener {

    private EditText etIp;
    private EditText etPort;
    private EditText etUsername;
    private Button btnConnect;
    private TextView tvStatus;

    private final NetworkClient net = NetworkClient.getInstance();
    private String username;

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
        tvStatus = findViewById(R.id.tvStatus);

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
        if (MessageProtocol.LOGIN_RESPONSE.equals(MessageProtocol.getType(line))) {
            openLobby();
        }
    }

    private void openLobby() {
        Intent intent = new Intent(this, LobbyActivity.class);
        intent.putExtra(LobbyActivity.EXTRA_USERNAME, username);
        startActivity(intent);
        finish();
    }

    private void setStatus(String text) {
        tvStatus.setText(text);
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
