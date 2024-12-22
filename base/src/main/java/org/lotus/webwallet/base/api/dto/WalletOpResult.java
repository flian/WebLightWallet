package org.lotus.webwallet.base.api.dto;

/**
 * @author : foy
 * @date : 2024/12/22:15:26
 **/
public class WalletOpResult<T> {
    private WalletOpResultEnum code;
    private String message;

    private T Data;
}
