package de.tudarmstadt.informatik.tk.assistance.event.module;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 03.01.2016
 */
public class ModuleOptionalPermissionEvent {

    private boolean isGranted;

    private int position;

    public ModuleOptionalPermissionEvent(boolean isGranted, int position) {
        this.position = position;
        this.isGranted = isGranted;
    }

    public boolean isGranted() {
        return this.isGranted;
    }

    public int getPosition() {
        return this.position;
    }

    @Override
    public String toString() {
        return "ModuleOptionalPermissionEvent{" +
                "isGranted=" + isGranted +
                ", position=" + position +
                '}';
    }
}