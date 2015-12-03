package de.tudarmstadt.informatik.tk.android.assistance.controller;

import java.util.List;

import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbUser;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public interface CommonController {

    List<DbModule> getAllActiveModules(Long userId);

    DbUser getUserByToken(String userToken);

    DbUser getUserByEmail(String userEmail);
}
