package de.tudarmstadt.informatik.tk.android.assistance.handler;

import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbUser;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 03.12.2015
 */
public interface OnUserNotFoundInDbHandler {

    void onUserFound(DbUser user);

    void onUserNotFound();
}
