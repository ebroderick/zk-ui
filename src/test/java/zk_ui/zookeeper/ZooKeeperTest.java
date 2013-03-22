package zk_ui.zookeeper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZooKeeperTest extends ZooKeeperTestSupport implements Watcher {
    private static Logger logger = LoggerFactory.getLogger(ZooKeeperTest.class);

    @Before
    public void before() throws Exception {
        startZooKeeper();
    }

    @After
    public void after() throws Exception {
        stopZooKeeper();
    }

    @Test
    public void testAddNode() throws Exception {
        ZooKeeper zkClient = getClient(this);
        String testPath = "/test";
        String testValue = "testValue";

        Stat stat = zkClient.exists(testPath, false);
        assertNull(stat);

        assertEquals(testPath, zkClient.create(testPath, testValue.getBytes(), ZooDefs.Ids.READ_ACL_UNSAFE,
                CreateMode.PERSISTENT));

        stat = zkClient.exists(testPath, false);
        assertNotNull(stat);
        assertEquals(testValue, new String(zkClient.getData(testPath, false, stat)));
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        logger.info("got event: {}", watchedEvent);
    }
}
