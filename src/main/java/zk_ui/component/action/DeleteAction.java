package zk_ui.component.action;

import com.vaadin.event.Action;
import com.vaadin.ui.UI;
import org.vaadin.dialogs.ConfirmDialog;
import zk_ui.component.ZkTreeLayout;
import zk_ui.zookeeper.ZkClient;
import zk_ui.zookeeper.ZkNode;

public class DeleteAction extends Action implements Action.Listener {
    private ZkTreeLayout zkTreeLayout;
    private ZkClient zkClient;

    public DeleteAction(String caption, ZkTreeLayout zkTreeLayout, ZkClient zkClient) {
        super(caption);
        this.zkTreeLayout = zkTreeLayout;
        this.zkClient = zkClient;
    }

    @Override
    public void handleAction(Object sender, final Object target) {
        ConfirmDialog.show(UI.getCurrent(), "Please Confirm: ", "Are you sure you want to delete " +
                ((ZkNode) target).getFullPath() + "?", "Yes", "No",
                new ConfirmDialog.Listener() {
                    @Override
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            zkClient.deleteNode((ZkNode) target);
                            zkTreeLayout.removeNodeChildren(((ZkNode) target));
                            zkTreeLayout.removeNode((ZkNode) target);
                        }
                    }
                });
    }
}
