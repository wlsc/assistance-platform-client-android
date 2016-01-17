package de.tudarmstadt.informatik.tk.assistance.event.module;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 10.01.2016
 */
public class UpdateModuleAllowedCapabilityStateEvent {

    private int type;

    private boolean isChecked;

    public UpdateModuleAllowedCapabilityStateEvent(int type, boolean isChecked) {
        this.type = type;
        this.isChecked = isChecked;
    }

    public int getType() {
        return this.type;
    }

    public boolean isChecked() {
        return this.isChecked;
    }

    @Override
    public String toString() {
        return "UpdateModuleAllowedCapabilityStateEvent{" +
                "type=" + type +
                ", isChecked=" + isChecked +
                '}';
    }
}