package zk_ui;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zk_ui.component.ZkNodeDetailLayout;
import zk_ui.component.ZkTreeLayout;
import zk_ui.component.ZkUiMenuBar;
import zk_ui.config.ConfigurationManager;
import zk_ui.config.ZkServer;
import zk_ui.zookeeper.ZkClient;
import zk_ui.zookeeper.ZkHost;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class ZkUI extends UI {
    private static Logger logger = LoggerFactory.getLogger(ZkUI.class);

    @Override
    protected void init(VaadinRequest request) {
        ConfigurationManager configurationManager;
        try {
            configurationManager = new ConfigurationManager();
        } catch (ConfigurationManager.ConfigurationException e) {
            throw new RuntimeException(e);
        }

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.addComponent(new ZkUiMenuBar(configurationManager));

        HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
        splitPanel.setSplitPosition(300, Unit.PIXELS);

        ZkClient zkClient = getZkClient(configurationManager);

        ZkNodeDetailLayout nodeDetailLayout = new ZkNodeDetailLayout(zkClient);
        ZkTreeLayout zkTreeLayout = new ZkTreeLayout(zkClient, nodeDetailLayout);

        splitPanel.setFirstComponent(zkTreeLayout);
        splitPanel.setSecondComponent(nodeDetailLayout);

        content.addComponent(splitPanel);
        content.setExpandRatio(splitPanel , 1.0f);

        setContent(content);
    }

    private ZkClient getZkClient(ConfigurationManager configurationManager) {
        List<ZkServer> zkServers = configurationManager.getZkServers();
        List<ZkHost> zkHosts = new ArrayList<ZkHost>();

        for (ZkServer zkServer : zkServers) {
            zkHosts.add(new ZkHost(zkServer));
        }

        return new ZkClient(zkHosts);
    }
}
