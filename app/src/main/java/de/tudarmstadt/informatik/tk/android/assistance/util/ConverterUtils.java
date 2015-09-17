package de.tudarmstadt.informatik.tk.android.assistance.util;

import java.util.Date;
import java.util.Locale;

import de.tudarmstadt.informatik.tk.android.assistance.model.api.module.AvailableModuleResponse;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.module.ModuleCapabilityResponse;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbModule;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.android.kraken.utils.DateUtils;

/**
 * Converter between various models
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 08.09.2015
 */
public class ConverterUtils {

    private ConverterUtils() {
    }

    /**
     * Converts from db module object to available module response object
     *
     * @param dbModule
     * @return
     */
    public static final AvailableModuleResponse convertModule(DbModule dbModule) {

        AvailableModuleResponse availableModuleResponse = new AvailableModuleResponse();

        availableModuleResponse.setTitle(dbModule.getTitle());
        availableModuleResponse.setLogo(dbModule.getLogoUrl());
        availableModuleResponse.setCopyright(dbModule.getCopyright());
        availableModuleResponse.setDescriptionShort(dbModule.getDescriptionShort());
        availableModuleResponse.setDescriptionFull(dbModule.getDescriptionFull());
        availableModuleResponse.setModulePackage(dbModule.getPackageName());
        availableModuleResponse.setSupportEmail(dbModule.getSupportEmail());

        return availableModuleResponse;
    }

    /**
     * Converts from available module response object to db module object
     *
     * @param availableModuleResponse
     * @return
     */
    public static final DbModule convertModule(AvailableModuleResponse availableModuleResponse) {

        DbModule dbModule = new DbModule();

        dbModule.setTitle(availableModuleResponse.getTitle() == null ? "" : availableModuleResponse.getTitle());
        dbModule.setLogoUrl(availableModuleResponse.getLogo() == null ? "" : availableModuleResponse.getLogo());
        dbModule.setCopyright(availableModuleResponse.getCopyright() == null ? "" : availableModuleResponse.getCopyright());
        dbModule.setDescriptionShort(availableModuleResponse.getDescriptionShort() == null ? "" : availableModuleResponse.getDescriptionShort());
        dbModule.setDescriptionFull(availableModuleResponse.getDescriptionFull() == null ? "" : availableModuleResponse.getDescriptionFull());
        dbModule.setPackageName(availableModuleResponse.getModulePackage() == null ? "" : availableModuleResponse.getModulePackage());
        dbModule.setSupportEmail(availableModuleResponse.getSupportEmail() == null ? "" : availableModuleResponse.getSupportEmail());
        dbModule.setCreated(DateUtils.dateToISO8601String(new Date(), Locale.getDefault()));

        return dbModule;
    }

    /**
     * Converts from db module capability object to module capability response object
     *
     * @param moduleCapability
     * @return
     */
    public static final ModuleCapabilityResponse convertModuleCapability(DbModuleCapability moduleCapability) {

        ModuleCapabilityResponse moduleCapabilityResponse = new ModuleCapabilityResponse();

        moduleCapabilityResponse.setType(moduleCapability.getType());
        moduleCapabilityResponse.setCollectionFrequency(moduleCapability.getCollectionFrequency());
        moduleCapabilityResponse.setRequiredUpdateFrequency(moduleCapability.getRequiredUpdateFrequency());
        moduleCapabilityResponse.setMinRequiredReadingsOnUpdate(moduleCapability.getMinRequiredReadingsOnUpdate());

        return moduleCapabilityResponse;
    }

    /**
     * Converts from module capability response object to db module capability object
     *
     * @param moduleCapabilityResponse
     * @return
     */
    public static final DbModuleCapability convertModuleCapability(ModuleCapabilityResponse moduleCapabilityResponse) {

        DbModuleCapability moduleCapability = new DbModuleCapability();

        moduleCapability.setType(moduleCapabilityResponse.getType());
        moduleCapability.setCollectionFrequency(moduleCapabilityResponse.getCollectionFrequency());
        moduleCapability.setRequiredUpdateFrequency(moduleCapabilityResponse.getRequiredUpdateFrequency());
        moduleCapability.setMinRequiredReadingsOnUpdate(moduleCapabilityResponse.getMinRequiredReadingsOnUpdate());
        moduleCapability.setCreated(DateUtils.dateToISO8601String(new Date(), Locale.getDefault()));

        return moduleCapability;
    }
}
