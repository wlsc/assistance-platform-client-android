package de.tudarmstadt.informatik.tk.android.assistance.util;

import java.util.Date;
import java.util.Locale;

import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.module.AvailableModuleResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.module.ModuleCapabilityResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.DateUtils;

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
    public static AvailableModuleResponseDto convertModule(DbModule dbModule) {

        if (dbModule == null) {
            return null;
        }

        AvailableModuleResponseDto availableModuleResponse = new AvailableModuleResponseDto();

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
    public static DbModule convertModule(AvailableModuleResponseDto availableModuleResponse) {

        if (availableModuleResponse == null) {
            return null;
        }

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
    public static ModuleCapabilityResponseDto convertModuleCapability(DbModuleCapability moduleCapability) {

        if (moduleCapability == null) {
            return null;
        }

        ModuleCapabilityResponseDto moduleCapabilityResponse = new ModuleCapabilityResponseDto();

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
    public static DbModuleCapability convertModuleCapability(ModuleCapabilityResponseDto moduleCapabilityResponse) {

        if (moduleCapabilityResponse == null) {
            return null;
        }

        DbModuleCapability moduleCapability = new DbModuleCapability();

        moduleCapability.setType(moduleCapabilityResponse.getType());
        moduleCapability.setCollectionFrequency(moduleCapabilityResponse.getCollectionFrequency());
        moduleCapability.setRequiredUpdateFrequency(moduleCapabilityResponse.getRequiredUpdateFrequency());
        moduleCapability.setMinRequiredReadingsOnUpdate(moduleCapabilityResponse.getMinRequiredReadingsOnUpdate());
        moduleCapability.setCreated(DateUtils.dateToISO8601String(new Date(), Locale.getDefault()));

        return moduleCapability;
    }
}
