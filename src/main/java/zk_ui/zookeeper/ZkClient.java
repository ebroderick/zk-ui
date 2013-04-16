package zk_ui.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
            ZkNode rootNode = new ZkNode(zkHost, zkHost.getName(), "/", true, null, stat.getNumChildren(),
                    new Date(stat.getCtime()), new Date(stat.getMtime()), stat.getVersion());

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
            byte[] byteValue = zookeepers.get(zkHost).getData(path, false, stat);
            if (byteValue != null) {
                value = new String(byteValue);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return value;
    }

    public String updateValue(ZkNode zkNode, String newValue) {
        String value = null;
        try {
            zookeepers.get(zkNode.getZkHost()).setData(zkNode.getFullPath(),
                    newValue.getBytes("UTF-8"), -1);
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

        return createZKNode(parentNode, nodeName);
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
                    children.add(createZKNode(zkNode, path));
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return children;
    }

    private ZkNode createZKNode(ZkNode parent, String nodeName) {
        String fullPath = getFullPath(parent, nodeName);
        Stat stat = getStat(fullPath, parent.getZkHost());
        String value = getValue(fullPath, parent.getZkHost(), stat);
        int numOfChildren = stat.getNumChildren();
        long createTime = stat.getCtime();
        long modifiedTime = stat.getMtime();
        int version = stat.getVersion();

        return new ZkNode(parent.getZkHost(), nodeName, fullPath, false, value, numOfChildren, new Date(createTime),
                new Date(modifiedTime), version);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        logger.info("got event: " + watchedEvent.toString());
    }
}
