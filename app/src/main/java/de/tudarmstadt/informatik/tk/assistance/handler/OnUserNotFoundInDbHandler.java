package de.tudarmstadt.informatik.tk.assistance.handler;

import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbUser;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 03.12.2015
 */
public interface OnUserNotFoundInDbHandler {

    void onUserFound(DbUser user);

    void onUserNotFound();
}
