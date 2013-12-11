package zk_ui.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.LayoutEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zk_ui.zookeeper.ZkClient;
import zk_ui.zookeeper.ZkNode;

public class ZkNodeDetailLayout extends VerticalLayout implements ItemClickEvent.ItemClickListener {
    private static Logger logger = LoggerFactory.getLogger(ZkTreeLayout.class);
    private static final String CONTAINER_PROPERTY_NAME = "name";
    private static final String CONTAINER_PROPERTY_VALUE = "value";
    private static final String CONTAINER_PROPERTY_CREATE_TIMESTAMP = "createTimestamp";
    private static final String CONTAINER_PROPERTY_MODIFIED_TIMESTAMP = "lastModifiedTimestamp";
    private static final String CONTAINER_PROPERTY_VERSION = "version";
    private Table table;
    private Label pathLabel;
    private ZkClient zkClient;

    public ZkNodeDetailLayout(final ZkClient zkClient) {
        this.zkClient = zkClient;

        setSizeFull();

        table = new Table();
        table.setContainerDataSource(getContainer());

        table.setVisibleColumns(new Object[] {
                CONTAINER_PROPERTY_NAME,
                CONTAINER_PROPERTY_VALUE,
                CONTAINER_PROPERTY_CREATE_TIMESTAMP,
                CONTAINER_PROPERTY_MODIFIED_TIMESTAMP,
                CONTAINER_PROPERTY_VERSION});

        table.setColumnHeaders(new String[] { "Node Name", "Node Value", "Create Time", "Last Modified Time", "Version"});

        table.setSizeFull();
        table.setSelectable(true);
        table.setMultiSelect(true);
        table.setImmediate(true);
        table.setColumnReorderingAllowed(true);
        table.setColumnCollapsingAllowed(true);
        table.setColumnWidth(CONTAINER_PROPERTY_VALUE, 400);
        table.setColumnCollapsed(CONTAINER_PROPERTY_VERSION, true);
        table.setTableFieldFactory(new ZkNodeDetailFieldFactory());
        table.setEditable(true);

        addComponent(table);
        setExpandRatio(table, 1.0f);

        pathLabel = new Label();
        addComponent(pathLabel);
        pathLabel.setContentMode(ContentMode.HTML);
        setComponentAlignment(pathLabel, Alignment.BOTTOM_LEFT);
    }

    /**
     * Click listener for tree
     * @param itemClickEvent
     */
    @Override
    public void itemClick(ItemClickEvent itemClickEvent) {
        logger.debug("ItemClickEvent! (" + itemClickEvent.getItemId().getClass().getName() + ")");
        Object itemId = itemClickEvent.getItemId();
        if (itemId instanceof ZkNode) {
            ZkNode zkNode = (ZkNode) itemId;
            table.setContainerDataSource(getContainer(zkNode));
        }
        setPathLabel(((ZkNode) itemId).getFullPath());
    }

    public void showChildren(ZkNode zkNode) {
        table.setContainerDataSource(getContainerForChildren(zkNode));
        setPathLabel(zkNode.getFullPath());
    }

    private void setPathLabel(String path) {
        pathLabel.setValue("<span style=\"font-size:10px;\"><b>Path:</b> " + path + "</span>");
    }

    public void clear() {
        table.setContainerDataSource(getContainer(new ArrayList<ZkNode>()));
    }

    private IndexedContainer getContainer() {
        return getContainer(new ArrayList<ZkNode>());
    }

    private IndexedContainer getContainer(ZkNode zkNode) {
        return getContainer(Arrays.asList(new ZkNode[]{zkNode}));
    }

    private IndexedContainer getContainerForChildren(ZkNode zkNode) {
        return getContainer(zkClient.getChildren(zkNode));
    }

    private IndexedContainer getContainer(List<ZkNode> zkNodes) {
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(CONTAINER_PROPERTY_NAME, String.class, null);
        container.addContainerProperty(CONTAINER_PROPERTY_VALUE, Layout.class, null);
        container.addContainerProperty(CONTAINER_PROPERTY_CREATE_TIMESTAMP, Date.class, null);
        container.addContainerProperty(CONTAINER_PROPERTY_MODIFIED_TIMESTAMP, Date.class, null);
        container.addContainerProperty(CONTAINER_PROPERTY_VERSION, Integer.class, null);

        for (ZkNode zkNode : zkNodes) {
            Item item = container.addItem(zkNode.getFullPath());
            item.getItemProperty(CONTAINER_PROPERTY_NAME).setValue(zkNode.getNodeName());
            item.getItemProperty(CONTAINER_PROPERTY_VALUE).setValue(
                    new ZkNodeValueEditLayout(zkNode, container));
            item.getItemProperty(CONTAINER_PROPERTY_CREATE_TIMESTAMP).setValue(zkNode.getCreateTimestamp());
            item.getItemProperty(CONTAINER_PROPERTY_MODIFIED_TIMESTAMP).setValue(zkNode.getModifiedTimestamp());
            item.getItemProperty(CONTAINER_PROPERTY_VERSION).setValue(zkNode.getVersion());
        }

        return container;
    }

    private class ZkNodeDetailFieldFactory extends DefaultFieldFactory {
        public Field createField(Container container, final Object itemId, Object propertyId, Component uiContext) {
            if (!CONTAINER_PROPERTY_VALUE.equals(propertyId)) {
                TextField field = new TextField((String) propertyId);
                field.setSizeFull();
                field.setReadOnly(true);
                return field;
            } else {
                return super.createField(container, itemId, propertyId, uiContext);
            }
        }
    }

    private class ZkNodeValueEditLayout extends VerticalLayout {
        private ZkNodeValueEditLayout(final ZkNode zkNode, final Container container) {
            final TextField textField = new TextField();
            textField.setSizeFull();
            textField.setValue(zkNode.getValue());
            textField.setReadOnly(true);

            textField.addShortcutListener(new ShortcutListener("EnterKey", ShortcutAction.KeyCode.ENTER, null) {
                @Override
                public void handleAction(Object sender, Object target) {
                    String value = ((TextField) target).getValue();
                    zkClient.updateValue(zkNode, value);
                    zkNode.refresh(zkClient);
                    ((TextField) target).setReadOnly(true);

                    container.getItem(zkNode.getFullPath())
                            .getItemProperty(CONTAINER_PROPERTY_MODIFIED_TIMESTAMP)
                            .setValue(zkNode.getModifiedTimestamp());

                    container.getItem(zkNode.getFullPath())
                            .getItemProperty(CONTAINER_PROPERTY_VERSION)
                            .setValue(zkNode.getVersion());
                }
            });

            addComponent(textField);
            addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
                @Override
                public void layoutClick(LayoutEvents.LayoutClickEvent layoutClickEvent) {
                    if (layoutClickEvent.isDoubleClick() && layoutClickEvent.getChildComponent() == textField) {
                        textField.setReadOnly(!textField.isReadOnly());
                    }
                }
            });
        }
    }

}
