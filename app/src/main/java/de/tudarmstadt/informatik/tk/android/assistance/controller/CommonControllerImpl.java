package de.tudarmstadt.informatik.tk.android.assistance.controller;

import android.content.Context;

import java.util.Collections;
import java.util.List;

import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.ApiProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.DaoProvider;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public class CommonControllerImpl implements CommonController {

    protected final DaoProvider daoProvider;

    protected final ApiProvider apiProvider;

    public CommonControllerImpl(Context context) {
        this.daoProvider = DaoProvider.getInstance(context);
        this.apiProvider = ApiProvider.getInstance(context);
    }

    @Override
    public List<DbModule> getAllActiveModules(String userToken) {

        if (userToken == null) {
            return Collections.emptyList();
        }

        DbUser user = daoProvider.getUserDao().getByToken(userToken);

        if (user == null) {
            return Collections.emptyList();
        }

        return daoProvider.getModuleDao().getAllActive(user.getId());
    }

    @Override
    public List<DbModule> getAllActiveModules(Long userId) {
        return daoProvider.getModuleDao().getAllActive(userId);
    }

    @Override
    public List<DbModuleCapability> getAllActiveModuleCapabilities(Long moduleId) {
        return daoProvider.getModuleCapabilityDao().getAllActive(moduleId);
    }

    @Override
    public List<DbModuleCapability> getAllActiveRequiredModuleCapabilities(Long moduleId) {
        return daoProvider.getModuleCapabilityDao().getAllActiveRequired(moduleId);
    }

    @Override
    public DbUser getUserByToken(String userToken) {
        return daoProvider
                .getUserDao()
                .getByToken(userToken);
    }

    @Override
    public DbUser getUserByEmail(String userEmail) {
        return daoProvider
                .getUserDao()
                .getByEmail(userEmail);
    }

    @Override
    public List<DbModule> getAllUserModules(String userToken) {

        DbUser user = daoProvider.getUserDao().getByToken(userToken);

        if (user == null) {
            return Collections.emptyList();
        }

        return daoProvider.getModuleDao().getAll(user.getId());
    }
}