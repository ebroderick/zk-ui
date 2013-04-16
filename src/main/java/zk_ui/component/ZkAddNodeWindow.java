package zk_ui.component;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zk_ui.zookeeper.ZkClient;
import zk_ui.zookeeper.ZkNode;

public class ZkAddNodeWindow extends Window {
    private static Logger logger = LoggerFactory.getLogger(ZkAddNodeWindow.class);

    public ZkAddNodeWindow(final Tree tree, final ZkClient zkClient, final ZkNode parentNode) {
        setCaption("Add a new znode...");
        setWidth(500, Unit.PIXELS);
        setHeight(350, Unit.PIXELS);
        center();

        FormLayout content = new FormLayout();
        setContent(content);

        content.addComponent(new Label("Node Name:"));

        final TextField nodeName = new TextField();
        nodeName.setImmediate(true);
        nodeName.setMaxLength(500);
        content.addComponent(nodeName);

        content.addComponent(new Label("Node Value:"));

        final TextField nodeValue = new TextField();
        nodeValue.setImmediate(true);
        nodeValue.setMaxLength(500);
        content.addComponent(nodeValue);

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
                String name = nodeName.getValue();
                String value = nodeName.getValue();

                logger.info("adding new node: " + name + ", " + value + " to path " + parentNode.getFullPath());

                ZkNode node = zkClient.addNode(parentNode, name, value);
                Item item = tree.addItem(node);
                item.getItemProperty(ZkTreeLayout.ITEM_CAPTION_PROPERTY).setValue(node.getNodeName());

                tree.setChildrenAllowed(parentNode, true);
                tree.setChildrenAllowed(node, false);
                tree.setParent(node, parentNode);

                ((HierarchicalContainer) tree.getContainerDataSource()).sort(
                        new Object[] {ZkTreeLayout.ITEM_CAPTION_PROPERTY}, new boolean[]{true});

                thisWindow.close();
            }
        }));

        content.addComponent(buttonLayout);
        content.setComponentAlignment(buttonLayout, Alignment.BOTTOM_RIGHT);
    }
}
