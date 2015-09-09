package de.tudarmstadt.informatik.tk.android.assistance.events;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 09.09.2015
 */
public class DrawerUpdateEvent {

    private String userEmail;

    public DrawerUpdateEvent() {
    }

    public DrawerUpdateEvent(String mUserEmail) {
        this.userEmail = mUserEmail;
    }

    public String getUserEmail() {
        return this.userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
