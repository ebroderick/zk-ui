package zk_ui;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.apache.zookeeper.server.NIOServerCnxn;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zk_ui.component.ZkNodeDetailLayout;
import zk_ui.component.ZkTreeLayout;
import zk_ui.component.ZkUiMenuBar;
import zk_ui.zookeeper.ZkClient;
import zk_ui.zookeeper.ZkHost;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Arrays;

@SuppressWarnings("serial")
public class ZkUI extends UI {
    private static final int ZK_PORT = 31235;
    private static Logger logger = LoggerFactory.getLogger(ZkUI.class);
    private NIOServerCnxn.Factory serverCnxnFactory;

    @Override
    protected void init(VaadinRequest request) {
        try {
            startEmbeddedZooKeeper();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.addComponent(new ZkUiMenuBar());

        HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
        splitPanel.setSplitPosition(300, Unit.PIXELS);

        ZkNodeDetailLayout nodeDetailLayout = new ZkNodeDetailLayout(getZkClient());
        ZkTreeLayout zkTreeLayout = new ZkTreeLayout(getZkClient(), nodeDetailLayout);

        splitPanel.setFirstComponent(zkTreeLayout);
        splitPanel.setSecondComponent(nodeDetailLayout);

        content.addComponent(splitPanel);
        content.setExpandRatio(splitPanel , 1.0f);

        setContent(content);
    }

    private ZkClient getZkClient() {
        ZkHost zkHost = new ZkHost("localhost", "localhost:" + ZK_PORT);
        return new ZkClient(Arrays.asList(new ZkHost[]{zkHost}));
    }

    private void startEmbeddedZooKeeper() throws Exception {
        String dataDirectory = System.getProperty("java.io.tmpdir");
        File dir = new File(dataDirectory, "zookeeper").getAbsoluteFile();

        ZooKeeperServer zkServer = new ZooKeeperServer(dir, dir, 2000);
        serverCnxnFactory = new NIOServerCnxn.Factory(new InetSocketAddress(ZK_PORT), 10);
        serverCnxnFactory.startup(zkServer);
    }

    @Override
    public void close() {
        super.close();
        serverCnxnFactory.shutdown();
    }
}
