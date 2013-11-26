package zk_ui.component.action;

import com.vaadin.event.Action;
import com.vaadin.ui.UI;
import org.vaadin.dialogs.ConfirmDialog;
import zk_ui.component.ZkTreeLayout;
import zk_ui.zookeeper.ZkClient;
import zk_ui.zookeeper.ZkNode;

public class PasteAction extends Action implements Action.Listener {
    private ZkTreeLayout zkTreeLayout;
    private ZkClient zkClient;

    public PasteAction(String caption, ZkTreeLayout zkTreeLayout, ZkClient zkClient) {
        super(caption);
        this.zkTreeLayout = zkTreeLayout;
        this.zkClient = zkClient;
    }

    @Override
    public void handleAction(Object sender, final Object target) {
        final ZkNode targetNode = ((ZkNode) target);
        final ZkNode sourceNode = zkTreeLayout.getZkNodeInClipboard();
        String htmlMessage = "Are you sure you want to copy this node?<ul>" +
                "<li><b>Source:</b>" + sourceNode.getZkHost().getHostAndPort() + sourceNode.getFullPath() + "</li>" +
                "<li><b>Target:</b>" + targetNode.getZkHost().getHostAndPort() + targetNode.getFullPath() + "</li></ul>";


        ConfirmDialog dialog = ConfirmDialog.show(UI.getCurrent(), "Please Confirm: ", htmlMessage, "Yes", "No",
                new ConfirmDialog.Listener() {
                    @Override
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {

                        }
                    }
                });
        dialog.setContentMode(ConfirmDialog.ContentMode.HTML);
        dialog.setHeight("16em");
    }
}
