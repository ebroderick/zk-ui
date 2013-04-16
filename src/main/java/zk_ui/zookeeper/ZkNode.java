package zk_ui.zookeeper;

import java.util.Date;

public class ZkNode {
    private ZkHost zkHost;
    private String nodeName;
    private String fullPath;
    private boolean root;
    private String value;
    private int numberOfChildren;
    private Date createTimestamp;
    private Date modifiedTimestamp;
    private int version;

    ZkNode(ZkHost zkHost, String nodeName, String fullPath, boolean root, String value, int numberOfChildren,
           Date createTimestamp, Date modifiedTimestamp, int version) {
        this.zkHost = zkHost;
        this.nodeName = nodeName;
        this.fullPath = fullPath;
        this.root = root;
        this.value = value;
        this.numberOfChildren = numberOfChildren;
        this.createTimestamp = createTimestamp;
        this.modifiedTimestamp = modifiedTimestamp;
    }

    public ZkHost getZkHost() {
        return zkHost;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getFullPath() {
        return fullPath;
    }

    public boolean isRoot() {
        return root;
    }

    public String getValue() {
        return value;
    }

    public int getNumberOfChildren() {
        return numberOfChildren;
    }

    public boolean hasChildren() {
        return numberOfChildren > 0;
    }

    public Date getCreateTimestamp() {
        return createTimestamp;
    }

    public Date getModifiedTimestamp() {
        return modifiedTimestamp;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZkNode zkNode = (ZkNode) o;

        if (fullPath != null ? !fullPath.equals(zkNode.fullPath) : zkNode.fullPath != null) return false;
        if (zkHost != null ? !zkHost.equals(zkNode.zkHost) : zkNode.zkHost != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = zkHost != null ? zkHost.hashCode() : 0;
        result = 31 * result + (fullPath != null ? fullPath.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ZkNode{" +
                "zkHost=" + zkHost +
                ", nodeName='" + nodeName + '\'' +
                ", fullPath='" + fullPath + '\'' +
                ", root=" + root +
                ", value='" + value + '\'' +
                ", numberOfChildren=" + numberOfChildren +
                '}';
    }
}
