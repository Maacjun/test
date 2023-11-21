package edu.jit.nsi.iot_ms.responseResult.exceptions;


import edu.jit.nsi.iot_ms.commons.util.StringUtil;
import edu.jit.nsi.iot_ms.responseResult.exceptions.BusinessExceptionEnum;
import edu.jit.nsi.iot_ms.responseResult.ResultCode;

public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 194906846739586856L;

    protected Integer code;

    protected String message;

    protected ResultCode resultCode;

    protected Object data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }

    public void setResultCode(ResultCode resultCode) {
        this.resultCode = resultCode;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public BusinessException() {
        BusinessExceptionEnum exceptionEnum = BusinessExceptionEnum.getByEClass(this.getClass());
        if (exceptionEnum != null) {
            resultCode = exceptionEnum.getResultCode();
            code = exceptionEnum.getResultCode().getCode();
            message = exceptionEnum.getResultCode().getMessage();
        }

    }

    public BusinessException(String message) {
        this();
        this.message = message;
    }

    public BusinessException(String format, Object... objects) {
        this();
        this.message = StringUtil.formatIfArgs(format, "{}", objects);
    }

    public BusinessException(ResultCode resultCode, Object data) {
        this(resultCode);
        this.data = data;
    }

    public BusinessException(ResultCode resultCode) {
        this.resultCode = resultCode;
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

}
