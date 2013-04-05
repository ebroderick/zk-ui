package zk_ui.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import zk_ui.zookeeper.ZkClient;
import zk_ui.zookeeper.ZkNode;

public class ZkNodeDetailLayout extends VerticalLayout implements ItemClickEvent.ItemClickListener {
    private static final String CONTAINER_PROPERTY_NAME = "name";
    private static final String CONTAINER_PROPERTY_VALUE = "value";
    private static final String CONTAINER_PROPERTY_NUM_OF_CHILDREN = "numberOfChildren";
    private static final String CONTAINER_PROPERTY_PATH = "path";

    private ZkClient zkClient;
    private Table table;

    public ZkNodeDetailLayout(ZkClient zkClient) {
        this.zkClient = zkClient;

        table = new Table();
        table.setSizeFull();
        table.setSelectable(true);
        table.setMultiSelect(true);
        table.setImmediate(true);

        table.setContainerDataSource(getContainer());
        table.setVisibleColumns(new Object[] {
                CONTAINER_PROPERTY_NAME,
                CONTAINER_PROPERTY_VALUE,
                CONTAINER_PROPERTY_NUM_OF_CHILDREN,
                CONTAINER_PROPERTY_PATH});

        table.setColumnReorderingAllowed(true);
        table.setColumnCollapsingAllowed(true);

        table.setColumnHeaders(new String[] { "Node Name", "Node Value", "Number of Child Nodes", "Node Path" });

        addComponent(table);
        setExpandRatio(table , 1.0f);
    }

    @Override
    public void itemClick(ItemClickEvent itemClickEvent) {
        Object itemId = itemClickEvent.getItemId();
        if (itemId instanceof ZkNode) {
            ZkNode zkNode = (ZkNode) itemId;
            table.setContainerDataSource(getContainer(zkNode));
        }

    }

    private IndexedContainer getContainer() {
        return getContainer(new ArrayList<ZkNode>());
    }

    private IndexedContainer getContainer(ZkNode zkNode) {
        return getContainer(Arrays.asList(new ZkNode[]{zkNode}));
    }

    private IndexedContainer getContainer(List<ZkNode> zkNodes) {
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(CONTAINER_PROPERTY_NAME, String.class, null);
        container.addContainerProperty(CONTAINER_PROPERTY_VALUE, String.class, null);
        container.addContainerProperty(CONTAINER_PROPERTY_NUM_OF_CHILDREN, Integer.class, null);
        container.addContainerProperty(CONTAINER_PROPERTY_PATH, String.class, null);

        for (ZkNode zkNode : zkNodes) {
            Item item = container.addItem(zkNode.getFullPath());
            item.getItemProperty(CONTAINER_PROPERTY_NAME).setValue(zkNode.getNodeName());
            item.getItemProperty(CONTAINER_PROPERTY_VALUE).setValue(zkNode.getValue());
            item.getItemProperty(CONTAINER_PROPERTY_NUM_OF_CHILDREN).setValue(zkNode.getNumberOfChildren());
            item.getItemProperty(CONTAINER_PROPERTY_PATH).setValue(zkNode.getFullPath());
        }

        return container;
    }
}
