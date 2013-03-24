package zk_ui.zookeeper;

import java.io.Serializable;

public class ZkHost implements Serializable {
    private String name;
    private String hostAndPort;

    public ZkHost(String name, String hostAndPort) {
        this.name = name;
        this.hostAndPort = hostAndPort;
    }

    public String getName() {
        return name;
    }

    public String getHostAndPort() {
        return hostAndPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZkHost zkHost = (ZkHost) o;

        if (hostAndPort != null ? !hostAndPort.equals(zkHost.hostAndPort) : zkHost.hostAndPort != null) return false;
        if (name != null ? !name.equals(zkHost.name) : zkHost.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (hostAndPort != null ? hostAndPort.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ZkHost{" +
                "name='" + name + '\'' +
                ", hostAndPort='" + hostAndPort + '\'' +
                '}';
    }
}
