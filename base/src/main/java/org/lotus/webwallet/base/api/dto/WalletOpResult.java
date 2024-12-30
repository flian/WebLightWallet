package org.lotus.webwallet.base.api.dto;

/**
 * @author : foy
 * @date : 2024/12/22:15:26
 **/
public class WalletOpResult<T> {
    private WalletOpResultEnum code;
    private String message;

    private T Data;

    public static <T> WalletOpResult<T> Ok(T data,String message){
        WalletOpResult<T> result = new WalletOpResult<>();
        result.code = WalletOpResultEnum.SUCCESS;
        result.Data = data;
        result.message = message;
        return result;
    }
    public static <T> WalletOpResult<T> fail(String message){
        WalletOpResult<T> result = new WalletOpResult<>();
        result.code = WalletOpResultEnum.FAIL;
        result.message = message;
        return result;
    }
    public static <T> WalletOpResult<T> fail(WalletOpResultEnum failType,String message){
        WalletOpResult<T> result = new WalletOpResult<>();
        result.code = failType;
        result.message = message;
        return result;
    }
}
