package de.tudarmstadt.informatik.tk.android.assistance.presenter.module.settings;

import android.content.Context;

import java.util.List;

import de.tudarmstadt.informatik.tk.android.assistance.controller.module.settings.ModuleSettingsController;
import de.tudarmstadt.informatik.tk.android.assistance.controller.module.settings.ModuleSettingsControllerImpl;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.CommonPresenterImpl;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.PreferenceProvider;
import de.tudarmstadt.informatik.tk.android.assistance.view.ModuleSettingsView;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 25.12.2015
 */
public class ModuleSettingsPresenterImpl extends
        CommonPresenterImpl implements
        ModuleSettingsPresenter {

    private static final String TAG = ModuleSettingsPresenterImpl.class.getSimpleName();

    private ModuleSettingsView view;
    private ModuleSettingsController controller;

    public ModuleSettingsPresenterImpl(Context context) {
        super(context);
        setController(new ModuleSettingsControllerImpl(this));
    }

    @Override
    public void doInitView() {

        view.initView();

        String userToken = PreferenceProvider.getInstance(getContext()).getUserToken();

        List<DbModule> allModules = controller.getAllUserModules(userToken);

        view.setAdapter(allModules);
    }

    @Override
    public void setView(ModuleSettingsView view) {
        super.setView(view);
        this.view = view;
    }

    @Override
    public void setController(ModuleSettingsController controller) {
        this.controller = controller;
    }

    @Override
    public void handleModuleCapabilityStateChanged(DbModuleCapability moduleCapability) {

        if (moduleCapability == null) {
            return;
        }

        controller.updateModuleCapability(moduleCapability);
    }
}
