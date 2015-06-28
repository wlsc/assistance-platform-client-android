package de.tu_darmstadt.tk.android.assistance.models.http;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP error codes
 * More info: https://github.com/Telecooperation/server_platform_assistance/wiki/API#client-erorrs
 *
 * Created by Wladimir Schmidt on 28.06.2015.
 */
public class HttpErrorCode {

    private static final Map<Integer, ErrorCode> codeMap = new HashMap<>();

    static {
        for (ErrorCode type : ErrorCode.values()) {
            codeMap.put(type.getCode(), type);
        }
    }

    public static ErrorCode fromCode(int code) {
        return codeMap.get(code);
    }

    public enum ErrorCode {
        LOGIN_NO_VALID(2),
        EMAIL_ALREADY_EXISTS(3),
        WRONG_PARAMETER_LIST(4),
        WRONG_MODULE_REQUIREMENTS(5),
        MODULE_ALREADY_EXISTS(6);

        private int code;

        private ErrorCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
