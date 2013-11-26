package zk_ui.component.action;

import com.vaadin.event.Action;
import com.vaadin.ui.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;
import zk_ui.component.ZkNodeDetailLayout;
import zk_ui.component.ZkTreeLayout;
import zk_ui.config.ConfigurationManager;
import zk_ui.zookeeper.ZkNode;

public class RemoveHostAction extends Action implements Action.Listener {
    private static Logger logger = LoggerFactory.getLogger(RemoveHostAction.class);

    private ConfigurationManager configurationManager;
    private ZkTreeLayout zkTreeLayout;
    private ZkNodeDetailLayout zkNodeDetailLayout;

    public RemoveHostAction(String caption, ConfigurationManager configurationManager, ZkTreeLayout zkTreeLayout,
            ZkNodeDetailLayout zkNodeDetailLayout) {

        super(caption);
        this.configurationManager = configurationManager;
        this.zkTreeLayout = zkTreeLayout;
        this.zkNodeDetailLayout = zkNodeDetailLayout;
    }

    @Override
    public void handleAction(Object sender, final Object target) {
        String name = ((ZkNode) target).getZkHost().getName();
        ConfirmDialog.show(UI.getCurrent(), "Please Confirm: ", "Are you sure you want to remove '" +
                name + "'?", "Yes", "No",
                new ConfirmDialog.Listener() {
                    @Override
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            try {
                                configurationManager.removeZkServer(((ZkNode) target).getZkHost());
                            } catch (ConfigurationManager.ConfigurationException e) {
                                logger.error(e.getMessage(), e);
                            }
                            zkTreeLayout.removeNodeChildren(((ZkNode) target));
                            zkTreeLayout.removeNode((ZkNode) target);
                            zkNodeDetailLayout.clear();
                        }
                    }
                });
    }
}
