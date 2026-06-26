package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	public int port;
	private ServerSocket serverSocket;
	private final GameService gameService = new GameService();

	public Server(int port) {
		this.port = port;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void acceptClients() {
		while (true) {
			try {
				Socket clientSocket = serverSocket.accept();
				ConnectedClient client = new ConnectedClient(clientSocket, gameService);
				Thread thr = new Thread(client);
				thr.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		Server server = new Server(9000);
		System.out.println("Server is listening on port 9000");
		server.acceptClients();
	}
}