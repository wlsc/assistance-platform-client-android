package de.tudarmstadt.informatik.tk.android.assistance.model.api;

/**
 * HTTP user error codes
 * More info: https://github.com/Telecooperation/server_platform_assistance/wiki/REST-API
 *
 * Created by Wladimir Schmidt on 28.06.2015.
 */
public class ApiHttpErrorCodes {

    public static final int LOGIN_NO_VALID = 2;

    public static final int EMAIL_ALREADY_EXISTS = 3;

    public static final int WRONG_PARAMETER_LIST = 4;

    public static final int WRONG_MODULE_REQUIREMENTS = 5;

    public static final int MODULE_ALREADY_ACTIVATED = 6;

    public static final int MODULE_NOT_ACTIVATED = 7;

    public static final int MODULE_NOT_EXISTS = 8;

    public static final int USER_ALREADY_LOGGED_IN = 9;

    public static final int MISSING_PARAMETERS = 10;

    public static final int INVALID_PARAMETERS = 11;

    public static final int DEVICE_ID_NOT_FOUND = 12;

    public static final int PLATFORM_NOT_SUPPORTED = 13;

    private ApiHttpErrorCodes() {
    }
}
