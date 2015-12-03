package de.tudarmstadt.informatik.tk.android.assistance.controller.modules;

import java.util.Set;

import de.tudarmstadt.informatik.tk.android.assistance.controller.CommonController;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnActiveModulesResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnAvailableModulesResponseHandler;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public interface ModulesController extends CommonController {

    /**
     * Request available modules from service
     */
    void requestAvailableModules(String userToken, OnAvailableModulesResponseHandler availableModulesHandler);

    /**
     * Request for active modules
     *
     * @param userToken
     * @param handler
     */
    void requestActiveModules(final String userToken,
                              final OnActiveModulesResponseHandler handler);

    /**
     * Check permissions is still granted to modules
     */
    Set<String> getGrantedPermissions();
}
