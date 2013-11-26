package zk_ui.component;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.Action;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zk_ui.component.action.DeleteAction;
import zk_ui.component.action.PasteAction;
import zk_ui.component.action.RemoveHostAction;
import zk_ui.config.ConfigurationManager;
import zk_ui.zookeeper.ZkClient;
import zk_ui.zookeeper.ZkNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ZkTreeLayout extends VerticalLayout implements Action.Handler {
    private static Logger logger = LoggerFactory.getLogger(ZkTreeLayout.class);
    protected static final String ITEM_CAPTION_PROPERTY = "nodeName";
    private static final String ITEM_ICON_PROPERTY = "icon";
    private static final Resource FOLDER_ICON = new ThemeResource("../runo/icons/16/folder.png");
    private static final Resource DOCUMENT_ICON = new ThemeResource("../runo/icons/16/document.png");
    private static final Action ADD_ACTION = new Action("Add Node");
    private static final Action SHOW_CHILDREN_ACTION = new Action("Show All Children");
    private static final Action COPY_ACTION = new Action("Copy");
    private static final String CLIPBOARD_PROPERTY = "clipboard";

    private ZkClient zkClient;
    private Tree tree;
    private ZkNodeDetailLayout nodeDetailLayout;
    private Action removeHostAction;
    private Action deleteAction;
    private Action pasteAction;

    public ZkTreeLayout(ZkClient zkClient, ZkNodeDetailLayout nodeDetailLayout, ConfigurationManager configurationManager) {
        this.zkClient = zkClient;
        this.nodeDetailLayout = nodeDetailLayout;

        tree = new Tree();

        tree.addContainerProperty(ITEM_CAPTION_PROPERTY, String.class, null);
        tree.addContainerProperty(ITEM_ICON_PROPERTY, Resource.class, null);

        tree.setItemIconPropertyId(ITEM_ICON_PROPERTY);
        tree.setItemCaptionPropertyId(ITEM_CAPTION_PROPERTY);
        tree.setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);
        tree.setImmediate(true);

        try {
            List<ZkNode> rootNodes = zkClient.getRoots();
            for (ZkNode rootNode : rootNodes) {
                Item root = tree.addItem(rootNode);
                root.getItemProperty(ITEM_CAPTION_PROPERTY).setValue(rootNode.getNodeName());
                root.getItemProperty(ITEM_ICON_PROPERTY).setValue(FOLDER_ICON);

                if (rootNode.hasChildren()) {
                    tree.setChildrenAllowed(rootNode, true);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        sort();

        addComponent(tree);
        setMargin(new MarginInfo(true, false, false, true));

        tree.addActionHandler(this);
        tree.addExpandListener(new ZkExpandListener());
        tree.addCollapseListener(new ZkCollapseListener());
        tree.addItemClickListener(nodeDetailLayout);

        removeHostAction = new RemoveHostAction("Remove Host", configurationManager, this, nodeDetailLayout);
        deleteAction = new DeleteAction("Delete Node", this, zkClient);
        pasteAction = new PasteAction("Paste", this, zkClient);
    }

    protected void addRoot(ZkNode rootNode) {
        Item root = tree.addItem(rootNode);
        root.getItemProperty(ITEM_CAPTION_PROPERTY).setValue(rootNode.getNodeName());
        root.getItemProperty(ITEM_ICON_PROPERTY).setValue(FOLDER_ICON);
        sort();
    }

    private void sort() {
        ((HierarchicalContainer) tree.getContainerDataSource()).sort(
                new Object[] {ZkTreeLayout.ITEM_CAPTION_PROPERTY}, new boolean[]{true});
    }

    @Override
    public Action[] getActions(Object target, Object sender) {
        ZkNode zkNode = (ZkNode) target;
        ArrayList<Action> actions = new ArrayList<Action>();

        if (zkNode != null) {
            if (zkNode.isRoot()) {
                actions.add(removeHostAction);
            }

            actions.add(ADD_ACTION);

            if (!zkNode.isRoot()) {
                actions.add(deleteAction);
            }

            if (zkNode.hasChildren()) {
                actions.add(SHOW_CHILDREN_ACTION);
            }

            if (!zkNode.isRoot()) {
                actions.add(COPY_ACTION);
            }

            if (getZkNodeInClipboard() != null) {
                actions.add(pasteAction);
            }
        }
        return actions.toArray(new Action[actions.size()]);
    }

    @Override
    public void handleAction(Action action, Object sender, final Object target) {
        logger.debug("handling action: " + action.toString());

        if (action == ADD_ACTION) {
            UI.getCurrent().addWindow(new ZkAddNodeWindow(tree, zkClient, (ZkNode) target));

        } else if (action == SHOW_CHILDREN_ACTION) {
            nodeDetailLayout.showChildren((ZkNode) target);

        } else if (action == COPY_ACTION) {
            setZkNodeInClipboard(((ZkNode) target));

        } else if (action instanceof Action.Listener) {
            ((Action.Listener) action).handleAction(sender, target);

        } else {
            logger.error("invalid action: " + action);
        }
    }

    public void removeNodeChildren(ZkNode zkNode) {
        removeNodeChildren(tree, zkNode);
    }

    public void removeNode(ZkNode zkNode) {
        tree.removeItem(zkNode);
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

                    if (child.hasChildren()) {
                        tree.setChildrenAllowed(child, true);
                        item.getItemProperty(ITEM_ICON_PROPERTY).setValue(FOLDER_ICON);
                    } else {
                        tree.setChildrenAllowed(child, false);
                        item.getItemProperty(ITEM_ICON_PROPERTY).setValue(DOCUMENT_ICON);
                    }

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

    public ZkNode getZkNodeInClipboard() {
        return (ZkNode) VaadinService.getCurrentRequest().getWrappedSession().getAttribute(CLIPBOARD_PROPERTY);
    }

    private void setZkNodeInClipboard(ZkNode zkNode) {
        VaadinService.getCurrentRequest().getWrappedSession().setAttribute(CLIPBOARD_PROPERTY, zkNode);
    }
}
