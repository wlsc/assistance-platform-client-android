package de.tu_darmstadt.tk.android.assistance.models.http;

/**
 * HTTP user error codes
 * More info: https://github.com/Telecooperation/server_platform_assistance/wiki/API#client-erorrs
 * <p>
 * Created by Wladimir Schmidt on 28.06.2015.
 */
public class HttpUserErrorCodes {

    public static final int LOGIN_NO_VALID = 2;

    public static final int EMAIL_ALREADY_EXISTS = 3;

    public static final int WRONG_PARAMETER_LIST = 4;

    public static final int WRONG_MODULE_REQUIREMENTS = 5;

    public static final int MODULE_ALREADY_EXISTS = 6;

    private HttpUserErrorCodes() {
    }
}
