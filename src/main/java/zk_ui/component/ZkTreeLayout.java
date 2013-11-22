package zk_ui.component;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.Action;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;
import zk_ui.zookeeper.ZkClient;
import zk_ui.zookeeper.ZkNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ZkTreeLayout extends VerticalLayout implements Action.Handler {
    private static Logger logger = LoggerFactory.getLogger(ZkTreeLayout.class);
    protected static final String ITEM_CAPTION_PROPERTY = "nodeName";
    private static final Action ADD_ACTION = new Action("Add Node");
    private static final Action DELETE_ACTION = new Action("Delete Node");
    private static final Action SHOW_CHILDREN_ACTION = new Action("Show All Children");

    private ZkClient zkClient;
    private Tree tree;
    private ZkNodeDetailLayout nodeDetailLayout;

    public ZkTreeLayout(ZkClient zkClient, ZkNodeDetailLayout nodeDetailLayout) {
        this.zkClient = zkClient;
        this.nodeDetailLayout = nodeDetailLayout;
        tree = new Tree();

        tree.addContainerProperty(ITEM_CAPTION_PROPERTY, String.class, null);
        tree.setItemCaptionPropertyId(ITEM_CAPTION_PROPERTY);
        tree.setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);
        tree.setImmediate(true);

        try {
            List<ZkNode> rootNodes = zkClient.getRoots();
            for (ZkNode rootNode : rootNodes) {
                Item root = tree.addItem(rootNode);
                root.getItemProperty(ITEM_CAPTION_PROPERTY).setValue(rootNode.getNodeName());

                if (rootNode.hasChildren()) {
                    tree.setChildrenAllowed(rootNode, true);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        ((HierarchicalContainer) tree.getContainerDataSource()).sort(
                new Object[] {ZkTreeLayout.ITEM_CAPTION_PROPERTY}, new boolean[]{true});

        addComponent(tree);
        setMargin(new MarginInfo(true, false, false, true));

        tree.addActionHandler(this);
        tree.addExpandListener(new ZkExpandListener());
        tree.addCollapseListener(new ZkCollapseListener());
        tree.addItemClickListener(nodeDetailLayout);
    }

    @Override
    public Action[] getActions(Object target, Object sender) {
        ZkNode zkNode = (ZkNode) target;
        ArrayList<Action> actions = new ArrayList<Action>();

        if (zkNode != null) {
            actions.add(ADD_ACTION);

            if (!zkNode.isRoot()) {
                actions.add(DELETE_ACTION);
            }

            if (zkNode.hasChildren()) {
                actions.add(SHOW_CHILDREN_ACTION);
            }
        }
        return actions.toArray(new Action[actions.size()]);
    }

    @Override
    public void handleAction(Action action, Object sender, final Object target) {
        logger.debug("handling action: " + action.toString());

        if (action == ADD_ACTION) {
            UI.getCurrent().addWindow(new ZkAddNodeWindow(tree, zkClient, (ZkNode) target));

        } else if (action == DELETE_ACTION) {
            ConfirmDialog.show(UI.getCurrent(), "Please Confirm: ", "Are you sure you want to delete " +
                ((ZkNode) target).getFullPath() + "?", "Yes", "No",
                new ConfirmDialog.Listener() {
                    @Override
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            zkClient.deleteNode((ZkNode) target);
                            removeNodeChildren(tree, ((ZkNode) target));
                            tree.removeItem(target);
                        }
                    }
                });

        } else if (action == SHOW_CHILDREN_ACTION) {
            nodeDetailLayout.showChildren((ZkNode) target);
        }
    }

    protected static void removeNodeChildren(Tree tree, ZkNode zkNode) {
        Collection<?> children = tree.getChildren(zkNode);
        List<ZkNode> zkNodeChildren = new ArrayList<ZkNode>();

        if (children != null) {
            for (Iterator<?> i = children.iterator(); i.hasNext();) {
                zkNodeChildren.add((ZkNode) i.next());
            }
            for (ZkNode child : zkNodeChildren) {
                removeNodeChildren(tree, child);
                tree.collapseItem(child);
                tree.removeItem(child);
            }
        }
    }

    private class ZkExpandListener implements Tree.ExpandListener {
        @Override
        public void nodeExpand(Tree.ExpandEvent expandEvent) {
            ZkNode node = (ZkNode) expandEvent.getItemId();
            if (node.hasChildren()) {
                List<ZkNode> children = zkClient.getChildren(node);
                for (ZkNode child : children) {
                    Item item = tree.addItem(child);
                    item.getItemProperty(ITEM_CAPTION_PROPERTY).setValue(child.getNodeName());

                    tree.setParent(child, node);
                    tree.setChildrenAllowed(child, child.hasChildren());

                    ((HierarchicalContainer) tree.getContainerDataSource()).sort(
                            new Object[] {ZkTreeLayout.ITEM_CAPTION_PROPERTY}, new boolean[]{true});
                }
            }
        }
    }

    private class ZkCollapseListener implements Tree.CollapseListener {
        @Override
        public void nodeCollapse(Tree.CollapseEvent collapseEvent) {
            removeNodeChildren(tree, (ZkNode) collapseEvent.getItemId());
        }
    }
}
