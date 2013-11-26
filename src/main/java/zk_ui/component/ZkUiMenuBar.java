package zk_ui.component;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import zk_ui.config.ConfigurationManager;
import zk_ui.zookeeper.ZkClient;

public class ZkUiMenuBar extends MenuBar {
    private final ConfigurationManager configurationManager;
    private final ZkTreeLayout zkTreeLayout;
    private final ZkClient zkClient;

    public ZkUiMenuBar(ConfigurationManager configurationManager, ZkTreeLayout zkTreeLayout, ZkClient zkClient) {
        this.configurationManager = configurationManager;
        this.zkTreeLayout = zkTreeLayout;
        this.zkClient = zkClient;

        setWidth(100.0f, Unit.PERCENTAGE);
        MenuBar.MenuItem fileItem = addItem("File", null);
        fileItem.setEnabled(true);

        MenuBar.MenuItem addZkServer = fileItem.addItem("Add ZooKeeper Server...", addZkServerCommand);
        addZkServer.setEnabled(true);
    }

    private final MenuBar.Command addZkServerCommand = new MenuBar.Command() {
        @Override
        public void menuSelected(final MenuBar.MenuItem selectedItem) {
            UI.getCurrent().addWindow(new ZkAddZkServerWindow(configurationManager, zkTreeLayout, zkClient));
        }
    };
}
