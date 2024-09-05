package at.ac.uibk.dps.lm;

import com.beust.jcommander.Parameter;

public class Arguments {

  @Parameter(names = "--zookeeper-connect-string", description = "The ZooKeeper connect string", required = true)
  public String zookeeperConnectString;

  @Parameter(names = "--address", description = "The address of this client", required = true)
  public String address;

  @Parameter(names = "--delay", description = "")
  public int delay = 1000;
}
