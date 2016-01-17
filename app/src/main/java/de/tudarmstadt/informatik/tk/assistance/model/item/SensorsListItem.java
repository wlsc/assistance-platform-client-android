package de.tudarmstadt.informatik.tk.assistance.model.item;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 17.07.2015
 */
public class SensorsListItem {

    private String name;

    private boolean enabled;

    private boolean visible;

    public SensorsListItem(String name) {
        this.name = name;
        this.enabled = true;
        this.visible = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
