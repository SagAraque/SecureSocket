import java.io.Serial;
import java.io.Serializable;

public class Request implements Serializable {

    @Serial
    private static final long serialVersionUID = 2906642554793891381L;
    public static String GET = "GET";
    public static String GET_DEVICES = "GET-DEVICES";

    public static String DELETE = "DELETE";
    public static String DELETE_USER = "DELETE-USER";
    public static String INSERT = "INSERT";
    public static String VERIFY = "VERIFY";
    public static String MOD = "MOD";
    public static String LOGIN = "LOGIN";
    public static String REGISTER = "REGISTER";
    public static String CLEAR_DEVICE = "CLEAR-DEVICE";
    public static String TOTP = "TOTP";
    private Object[] content = null;
    private String operationCode = null;

    public Request(Object[] object, String operationCode){
        this.content = object;
        this.operationCode = operationCode;
    }

    public Request(String operationCode){
        this.content = null;
        this.operationCode = operationCode;
    }


    public Request(){}

    public Object[] getContent() {
        return content;
    }

    public String getOperationCode() {
        return operationCode;
    }

}
