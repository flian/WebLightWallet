package org.lotus.webwallet.base.api.dto;

import lombok.Data;

/**
 * @author : foy
 * @date : 2025/1/3:10:49
 **/
@Data
public class ChangeWalletPasswordRequest extends WalletBaseRequest {
    private String newPassword;
}
