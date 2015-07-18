package de.tu_darmstadt.tk.android.assistance.models.items;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 17.07.2015
 */
public class SensorsListItem {

    private String name;

    public SensorsListItem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
