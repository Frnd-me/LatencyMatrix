package at.ac.uibk.dps.lm;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import java.io.IOException;

/**
 * Entry-point.
 */
public class Main {

  /**
   * Entry-point.
   *
   * @param argv CL arguments.
   */
  public static void main(String[] argv) {
    final var arguments = new Arguments();

    // Parse arguments
    final var jc = JCommander.newBuilder()
        .addObject(arguments)
        .build();

    try {
      jc.parse(argv);

      // Create echo server and thread
      final var echoServer = new EchoServer();
      final var echoServerThread = new Thread(echoServer);

      // Create latency matrix and thread
      final var latencyMatrix = new LatencyMatrix(arguments);
      final var latencyMatrixThread = new Thread(latencyMatrix);

      // CTRL+C hook
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        latencyMatrix.stop();
        echoServer.stop();
      }));

      // Start both threads
      echoServerThread.start();
      latencyMatrixThread.start();

      // Wait for both threads to finish
      latencyMatrixThread.join();
      echoServerThread.join();
    } catch (ParameterException e) {
      System.err.println(e.getMessage());
    } catch (IOException e) {
      System.err.printf("Could not start the echo server: %s\n", e.getMessage());
    } catch (InterruptedException ignored) {
    }
  }
}
