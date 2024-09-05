package at.ac.uibk.dps.lm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * A ping request, sends a ping request to a server and measures the RTT.
 */
public class PingRequest {

  /**
   * Round-trip time (RTT).
   */
  public final double rtt;

  /**
   * Initializes this ping request object, sends a PING packet to a server at address:port and
   * measures the RTT.
   *
   * @param address Address of server to ping.
   * @param port    Port of server to ping.
   */
  public PingRequest(String address, int port) {
    var rtt = -1.0;

    // Create socket, connect, send message, receive, and capture RTT
    try (Socket socket = new Socket()) {
      InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);

      socket.connect(inetSocketAddress, 1000);

      try (final var out = new PrintWriter(socket.getOutputStream());
          final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
        // Capture start time
        final var startTime = System.nanoTime();

        // Send message
        out.println("PING");
        out.flush();

        // Read response
        in.readLine();

        // Calculate delta time
        final var endTime = System.nanoTime();
        rtt = (endTime - startTime) / 1_000_000.0;
      }
    } catch (IOException e) {
      System.out.printf("Error during ping request: %f\n", e.getMessage());
    }

    this.rtt = rtt;
  }
}
