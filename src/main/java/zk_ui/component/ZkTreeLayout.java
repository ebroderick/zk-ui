package zk_ui.component;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;

public class ZkTreeLayout extends VerticalLayout {
    private static final String CAPTION_PROPERTY_ID = "name";

    public ZkTreeLayout() {
        Tree tree = new Tree();
        tree.setContainerDataSource(getMockContainer());
        tree.setItemCaptionPropertyId(CAPTION_PROPERTY_ID);
        tree.setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);
        addComponent(tree);
    }

    private  HierarchicalContainer getMockContainer() {
        Item item = null;
        int itemId = 0; // Increasing numbering for itemId:s

        String[][] hardware = {
                { "Desktops", "Dell OptiPlex GX240", "Dell OptiPlex GX260", "Dell OptiPlex GX280" },
                { "Monitors", "Benq T190HD", "Benq T220HD", "Benq T240HD" },
                { "Laptops", "IBM ThinkPad T40", "IBM ThinkPad T43", "IBM ThinkPad T60" }
        };

        HierarchicalContainer hwContainer = new HierarchicalContainer();
        hwContainer.addContainerProperty(CAPTION_PROPERTY_ID, String.class, null);
        for (int i = 0; i < hardware.length; i++) {
            // Add new item
            item = hwContainer.addItem(itemId);
            // Add name property for item
            item.getItemProperty(CAPTION_PROPERTY_ID).setValue(hardware[i][0]);
            // Allow children
            hwContainer.setChildrenAllowed(itemId, true);
            itemId++;
            for (int j = 1; j < hardware[i].length; j++) {
                // Add child items
                item = hwContainer.addItem(itemId);
                item.getItemProperty(CAPTION_PROPERTY_ID).setValue(hardware[i][j]);
                hwContainer.setParent(itemId, itemId - j);
                hwContainer.setChildrenAllowed(itemId, false);
                itemId++;
            }
        }
        return hwContainer;
    }
}
