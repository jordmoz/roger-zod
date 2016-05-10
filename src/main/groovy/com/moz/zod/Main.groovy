package com.moz.zod

import org.apache.curator.RetryPolicy
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.imps.DefaultACLProvider
import org.apache.curator.framework.recipes.leader.LeaderSelector
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter
import org.apache.curator.retry.BoundedExponentialBackoffRetry
import org.apache.mesos.Protos.FrameworkInfo

class Main {
  static void main(String [] args) {

    String cs = "192.168.99.100:2181"
    String mesosURL = "zk://${cs}/mesos"

    FrameworkInfo fi = FrameworkInfo.newBuilder()
        .setName("Zod")
        // .setHostname("dalstgmesos01")
        .setUser("root")
        .setRole("*")
        .setFailoverTimeout(0.0d)  // TODO(jord): Set higher in production.
        .build()

    RetryPolicy rp = new BoundedExponentialBackoffRetry(1000, 30000, Integer.MAX_VALUE)
    CuratorFramework cf = CuratorFrameworkFactory.builder()
        .retryPolicy(rp)
        .connectString(cs)
        .namespace("zod")
        //				.aclProvider(new DefaultACLProvider())
        .build()
    cf.start()
    cf.getZookeeperClient().blockUntilConnectedOrTimedOut()

    println "children: " + cf.getZookeeperClient().getZooKeeper().getChildren("/", false)

    LeaderSelector ls1 = startSelector("1", cf)
    LeaderSelector ls2 = startSelector("2", cf)

    while(true){
      Thread.sleep(500)
      // println "1 master:" + ls1.hasLeadership()
      // println "2 master:" + ls2.hasLeadership()
    }





    //		Zcheduler z = new Zcheduler();
    //		MesosSchedulerDriver driver = new MesosSchedulerDriver(z,
    //				fi,mesosURL, false)
    //
    //		//run the driver
    //		driver.run()

  }

  static LeaderSelector startSelector(String id, CuratorFramework cf) {
    LeaderSelectorListener lsl = new LeaderSelectorListenerAdapter() {
          @Override public void takeLeadership(CuratorFramework client) throws Exception {
            println "${id} now master"
            Thread.sleep(1000)
            println "${id} not master"
          }
        }
    LeaderSelector ls = new LeaderSelector(cf, "/leader", lsl);
    ls.autoRequeue()
    ls.setId(id)
    ls.start()
    return ls
  }
}
