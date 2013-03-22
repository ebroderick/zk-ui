package zk_ui.zookeeper;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.commons.io.FileUtils;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.server.NIOServerCnxn;
import org.apache.zookeeper.server.ZooKeeperServer;

public abstract class ZooKeeperTestSupport {
    private static final int ZK_PORT = 31235;
    private ZooKeeperServer zkServer;
    private NIOServerCnxn.Factory serverCnxnFactory;

    protected void startZooKeeper() throws IOException, InterruptedException {
        String dataDirectory = System.getProperty("java.io.tmpdir");
        File dir = new File(dataDirectory, "zookeeper").getAbsoluteFile();

        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }

        zkServer = new ZooKeeperServer(dir, dir, 2000);
        serverCnxnFactory = new NIOServerCnxn.Factory(new InetSocketAddress(ZK_PORT), 10);
        serverCnxnFactory.startup(zkServer);
    }

    protected void stopZooKeeper() {
        serverCnxnFactory.shutdown();
    }

    protected ZooKeeper getClient(Watcher watcher) throws IOException {
        return new ZooKeeper("localhost:" + ZK_PORT, 10000, watcher);
    }
}
