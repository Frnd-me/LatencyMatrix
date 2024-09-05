package at.ac.uibk.dps.lm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer implements Runnable {

  /**
   * Echo server port.
   */
  public static final int PORT = 0xCAFE;

  private final ServerSocket serverSocket;

  private volatile boolean running = true;

  /**
   * Initializes this echo server object, opening a server socket on PORT.
   *
   * @throws IOException
   */
  public EchoServer() throws IOException {
    serverSocket = new ServerSocket(PORT);
  }

  /**
   * Run this echo server.
   */
  @Override
  public void run() {
    try {
      while (running) {
        // Accept connection, receive and send back message
        try (Socket clientSocket = serverSocket.accept();
            final var in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
            final var out = new PrintWriter(clientSocket.getOutputStream())) {
          // Receive and send back
          out.println(in.readLine());
          out.flush();
        } catch (IOException e) {
          if (running) {
            System.err.printf("Error handling client connection: %s\n", e.getMessage());
          }
        }
      }
    } finally {
      try {
        serverSocket.close();
      } catch (IOException e) {
        System.err.printf("Error closing server socket: %s\n", e.getMessage());
      }
    }
  }

  /**
   * Stop this echo server.
   */
  public void stop() {
    running = false;

    try {
      serverSocket.close();
    } catch (IOException e) {
      System.err.printf("Error stopping server: %s\n", e.getMessage());
    }
  }
}
