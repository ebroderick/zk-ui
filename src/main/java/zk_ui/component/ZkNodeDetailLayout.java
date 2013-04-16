package zk_ui.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import zk_ui.zookeeper.ZkClient;
import zk_ui.zookeeper.ZkNode;

public class ZkNodeDetailLayout extends VerticalLayout implements ItemClickEvent.ItemClickListener {
    private static final String CONTAINER_PROPERTY_NAME = "name";
    private static final String CONTAINER_PROPERTY_VALUE = "value";
    private static final String CONTAINER_PROPERTY_CREATE_TIMESTAMP = "createTimestamp";
    private static final String CONTAINER_PROPERTY_MODIFIED_TIMESTAMP = "lastModifiedTimestamp";
    private static final String CONTAINER_PROPERTY_VERSION = "version";
    private static final String CONTAINER_PROPERTY_PATH = "path";

    private Table table;
    private Button saveButton;

    //maps the itemId (zk path) to its ZNode instance
    private Map<String, ZkNode> zkNodeMap = new HashMap<String, ZkNode>();

    //maps the itemId (zk path) to the text field for the znode value
    private Map<String, TextField> valueTextFields = new HashMap<String, TextField>();

    public ZkNodeDetailLayout(final ZkClient zkClient) {
        setSizeFull();

        table = new Table();
        table.setSizeFull();
        table.setSelectable(true);
        table.setMultiSelect(true);
        table.setImmediate(true);
        table.setColumnCollapsingAllowed(true);

        table.setContainerDataSource(getContainer());
        table.setVisibleColumns(new Object[] {
                CONTAINER_PROPERTY_NAME,
                CONTAINER_PROPERTY_VALUE,
                CONTAINER_PROPERTY_CREATE_TIMESTAMP,
                CONTAINER_PROPERTY_MODIFIED_TIMESTAMP,
                CONTAINER_PROPERTY_VERSION,
                CONTAINER_PROPERTY_PATH});

        table.setColumnHeaders(new String[] { "Node Name", "Node Value", "Create Time", "Last Modified Time", "Version",
                "Node Path" });

        table.setColumnReorderingAllowed(true);
        table.setColumnCollapsingAllowed(true);

        table.setTableFieldFactory(new TableFieldFactory() {
            public Field createField(Container container, final Object itemId, Object propertyId, Component uiContext) {
                TextField field = new TextField((String) propertyId);
                if (CONTAINER_PROPERTY_VALUE.equals(propertyId)) {
                    field.setData(itemId);
                    field.setSizeFull();
                    field.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.EAGER);
                    field.addTextChangeListener(new FieldEvents.TextChangeListener() {
                        @Override
                        public void textChange(FieldEvents.TextChangeEvent textChangeEvent) {
                            String originalValue = zkNodeMap.get(itemId).getValue();
                            System.out.println("checking originalValue for " + itemId + " (" + originalValue + ")");
                            if (!originalValue.equals(textChangeEvent.getText())) {
                                table.setColumnHeader(CONTAINER_PROPERTY_VALUE, "Node Value (edited)");
                                saveButton.setEnabled(true);
                            } else {
                                table.setColumnHeader(CONTAINER_PROPERTY_VALUE, "Node Value");
                                saveButton.setEnabled(false);
                            }
                        }
                    });
                    valueTextFields.put((String) itemId, field);
                } else {
                    field.setReadOnly(true);
                }
                return field;
            }
        });

        table.setEditable(true);
        addComponent(table);
        setExpandRatio(table, 1.0f);

        saveButton = new Button("Save");
        saveButton.setEnabled(false);
        saveButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                for (Object itemId : table.getContainerDataSource().getItemIds()) {
                    String zNodePath = (String) itemId;
                    ZkNode zNode = zkNodeMap.get(zNodePath);
                    TextField textField = valueTextFields.get(zNodePath);

                    if (!zNode.getValue().equals(textField.getValue())) {
                        zkClient.updateValue(zNode, textField.getValue());
                    }
                }

                Notification.show("Save button clicked!");
            }
        });

        addComponent(saveButton);
        setComponentAlignment(saveButton, Alignment.BOTTOM_RIGHT);
    }

    /**
     * Click listener for tree
     * @param itemClickEvent
     */
    @Override
    public void itemClick(ItemClickEvent itemClickEvent) {
        Object itemId = itemClickEvent.getItemId();
        if (itemId instanceof ZkNode) {
            //clear out znode value maps since we're loading new znodes
            zkNodeMap.clear();
            valueTextFields.clear();

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
        container.addContainerProperty(CONTAINER_PROPERTY_CREATE_TIMESTAMP, Date.class, null);
        container.addContainerProperty(CONTAINER_PROPERTY_MODIFIED_TIMESTAMP, Date.class, null);
        container.addContainerProperty(CONTAINER_PROPERTY_VERSION, Integer.class, null);
        container.addContainerProperty(CONTAINER_PROPERTY_PATH, String.class, null);

        for (ZkNode zkNode : zkNodes) {
            Item item = container.addItem(zkNode.getFullPath());
            item.getItemProperty(CONTAINER_PROPERTY_NAME).setValue(zkNode.getNodeName());
            item.getItemProperty(CONTAINER_PROPERTY_VALUE).setValue(zkNode.getValue());
            item.getItemProperty(CONTAINER_PROPERTY_CREATE_TIMESTAMP).setValue(zkNode.getCreateTimestamp());
            item.getItemProperty(CONTAINER_PROPERTY_MODIFIED_TIMESTAMP).setValue(zkNode.getModifiedTimestamp());
            item.getItemProperty(CONTAINER_PROPERTY_VERSION).setValue(zkNode.getVersion());
            item.getItemProperty(CONTAINER_PROPERTY_PATH).setValue(zkNode.getFullPath());

            zkNodeMap.put(zkNode.getFullPath(), zkNode);
        }

        return container;
    }
}
