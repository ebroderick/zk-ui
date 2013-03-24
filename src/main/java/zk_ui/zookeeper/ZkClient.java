package zk_ui.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZkClient implements Watcher {
    private static Logger logger = LoggerFactory.getLogger(ZkClient.class);
    private Map<ZkHost, ZooKeeper> zookeepers;

    public ZkClient(List<ZkHost> zkHosts) {
        zookeepers = new HashMap<ZkHost, ZooKeeper>();
        for (ZkHost zkHost : zkHosts) {
            try {
                zookeepers.put(zkHost, new ZooKeeper(zkHost.getHostAndPort(), 10000, this));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public List<ZkNode> getRoots() {
        List<ZkNode> roots = new ArrayList<ZkNode>();
        for (ZkHost zkHost : zookeepers.keySet()) {
            Stat stat = getStat("/", zkHost);
            ZkNode rootNode = new ZkNode(zkHost, zkHost.getName(), "/", true, null, stat.getNumChildren());

            logger.debug("adding root node: " + rootNode);

            roots.add(rootNode);
        }
        return roots;
    }

    private Stat getStat(String path, ZkHost zkHost) {
        Stat stat = null;
        try {
            stat = zookeepers.get(zkHost).exists(path, false);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return stat;
    }

    private String getValue(String path, ZkHost zkHost, Stat stat) {
        String value = null;
        try {
            value = new String(zookeepers.get(zkHost).getData(path, false, stat));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return value;
    }

    private String getFullPath(ZkNode parent, String nodeName) {
        if (parent.isRoot()) {
            return parent.getFullPath() + nodeName;
        } else {
            return parent.getFullPath() + "/" + nodeName;
        }
    }

    public ZkNode addNode(ZkNode parentNode, String nodeName, String nodeValue) {
        ZooKeeper zk = zookeepers.get(parentNode.getZkHost());
        String fullPath = getFullPath(parentNode, nodeName);

        try {
            zk.create(fullPath, nodeValue.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return new ZkNode(parentNode.getZkHost(), nodeName, fullPath, false, nodeValue, 0);
    }

    public void deleteNode(ZkNode node) {
        List<ZkNode> children = getChildren(node);
        for (ZkNode child : children) {
            deleteNode(child);
        }
        Stat stat = getStat(node.getFullPath(), node.getZkHost());
        try {
            zookeepers.get(node.getZkHost()).delete(node.getFullPath(), stat.getVersion());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public List<ZkNode> getChildren(ZkNode zkNode) {
        logger.debug("getting children for " + zkNode);

        List<ZkNode> children = new ArrayList<ZkNode>();

        if (zkNode.hasChildren()) {
            ZooKeeper zk = zookeepers.get(zkNode.getZkHost());
            try {
                List<String> paths = zk.getChildren(zkNode.getFullPath(), false);
                for (String path : paths) {
                    String fullPath = getFullPath(zkNode, path);
                    Stat stat = getStat(fullPath, zkNode.getZkHost());
                    String value = getValue(fullPath, zkNode.getZkHost(), stat);
                    children.add(new ZkNode(zkNode.getZkHost(), path, fullPath, false, value,
                            stat.getNumChildren()));
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return children;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        logger.info("got event: " + watchedEvent.toString());
    }
}
