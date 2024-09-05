package at.ac.uibk.dps.lm;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class LatencyMatrix implements Runnable, CuratorCacheListener {

  private static final String CLIENTS_NODE = "/clients";

  private static final Pattern ADDRESS_PATTERN = Pattern.compile(
      "([0-9]{1,3}(?:\\.[0-9]{1,3}){3})");

  private final Arguments arguments;

  private volatile boolean running = true;

  private final ConcurrentLinkedQueue<String> knownClientAddresses = new ConcurrentLinkedQueue();

  /**
   * Initializes this latency matrix instance with arguments.
   *
   * @param arguments Arguments.
   */
  public LatencyMatrix(Arguments arguments) {
    this.arguments = arguments;
  }

  /**
   * Run this latency matrix instance.
   * <p>
   * This instance will establish a connection to a ZooKeeper server, watch for new clients
   * appearing or known clients disappearing. Known clients will be continually pinged at a
   * configured interval.
   */
  @Override
  public void run() {
    // Connect to ZooKeeper
    try (final var curatorFramework = CuratorFrameworkFactory.builder()
        .connectString(arguments.zookeeperConnectString)
        .retryPolicy(new ExponentialBackoffRetry(1000, 3))
        .connectionTimeoutMs(3000)
        .sessionTimeoutMs(3000)
        .build()) {
      curatorFramework.start();

      // Watch the clients node
      try (final var clientsNodeCache = CuratorCache.builder(curatorFramework, CLIENTS_NODE)
          .build()) {
        clientsNodeCache.listenable().addListener(this);
        clientsNodeCache.start();

        // Ping known clients
        while (running) {
          for (final var clientAddress : knownClientAddresses) {
            final var ping = new PingRequest(clientAddress, EchoServer.PORT);

            System.out.printf("Address: %s - RTT: %f\n", clientAddress, ping.rtt);
          }

          // Wait
          Thread.sleep(arguments.delay);
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      System.out.printf("Error in Curator framework: %s\n", e.getMessage());
    }
  }

  /**
   * Stop running.
   */
  public void stop() {
    running = false;
  }

  private void nodeCreated(String nodePath, byte[] data) {
    final var address = new String(data, StandardCharsets.UTF_8);

    final var matcher = ADDRESS_PATTERN.matcher(address);

    if (matcher.find()) {
      knownClientAddresses.add(matcher.group());

      System.out.printf("New client appeared: %s\n", address);
    }
  }

  private void nodeDeleted(String nodePath, byte[] data) {
    final var address = new String(data, StandardCharsets.UTF_8);

    final var matcher = ADDRESS_PATTERN.matcher(address);

    if (matcher.find()) {
      knownClientAddresses.remove(matcher.group());

      System.out.printf("Client disappeared: %s\n", address);
    }
  }

  /**
   * ZooKeeper node watcher listener.
   *
   * @param type    Type of event.
   * @param oldData Old data.
   * @param data    Current data.
   */
  @Override
  public void event(Type type, ChildData oldData, ChildData data) {
    final var nodePath = data.getPath();

    switch (type) {
      case NODE_CREATED -> nodeCreated(nodePath, data.getData());
      case NODE_DELETED -> nodeDeleted(nodePath, oldData.getData());
    }
  }
}