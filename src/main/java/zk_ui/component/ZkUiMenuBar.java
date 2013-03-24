package zk_ui.component;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;

public class ZkUiMenuBar extends MenuBar {

    public ZkUiMenuBar() {
        setWidth(100.0f, Unit.PERCENTAGE);
        MenuBar.MenuItem child = addItem("File", menuCommand);
        child.setEnabled(true);
    }

    private final MenuBar.Command menuCommand = new MenuBar.Command() {
        @Override
        public void menuSelected(final MenuBar.MenuItem selectedItem) {
            Notification.show("Action " + selectedItem.getText(), Notification.Type.TRAY_NOTIFICATION);
        }
    };

}
