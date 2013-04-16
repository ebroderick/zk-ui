package zk_ui.component;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zk_ui.config.ConfigurationManager;

public class ZkAddZkServerWindow extends Window {
    private static Logger logger = LoggerFactory.getLogger(ZkAddZkServerWindow.class);

    public ZkAddZkServerWindow(final ConfigurationManager configurationManager) {
        setCaption("Add a ZooKeeper Server...");
        setWidth(500, Unit.PIXELS);
        setHeight(350, Unit.PIXELS);
        center();

        FormLayout content = new FormLayout();
        setContent(content);

        content.addComponent(new Label("ZooKeeper Server Name:"));

        final TextField zkServerName = new TextField();
        zkServerName.setImmediate(true);
        zkServerName.setMaxLength(500);
        content.addComponent(zkServerName);

        content.addComponent(new Label("ZooKeeper Server Host Name:"));

        final TextField zkServerHostName = new TextField();
        zkServerHostName.setImmediate(true);
        zkServerHostName.setMaxLength(500);
        content.addComponent(zkServerHostName);

        content.addComponent(new Label("ZooKeeper Server Port:"));

        final TextField zkServerPort = new TextField();
        zkServerPort.setImmediate(true);
        zkServerPort.setMaxLength(10);
        content.addComponent(zkServerPort);

        HorizontalLayout buttonLayout = new HorizontalLayout();

        final Window thisWindow = this;
        buttonLayout.addComponent(new Button("Cancel", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                thisWindow.close();
            }
        }));

        buttonLayout.addComponent(new Button("Add", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                String name = zkServerName.getValue();
                String hostName = zkServerHostName.getValue();
                String port = zkServerPort.getValue();

                logger.info("adding new zk server: " + name + ", " + hostName + ", " + port);

                try {
                    configurationManager.addZkServer(name, hostName, port);
                    Notification.show("Added '" + name + "'", Notification.Type.TRAY_NOTIFICATION);
                } catch (ConfigurationManager.ConfigurationException e) {
                    Notification.show("Could not add '" + name + "': " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
                    logger.error(e.getMessage(), e);
                }

                thisWindow.close();
            }
        }));

        content.addComponent(buttonLayout);
        content.setComponentAlignment(buttonLayout, Alignment.BOTTOM_RIGHT);
    }
}
