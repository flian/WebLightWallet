package org.lotus.webwallet.base.api.dto;

import lombok.Data;

/**
 * @author : foy
 * @date : 2024/12/22:15:36
 **/
@Data
public class EnsureWalletRequest extends WalletBaseRequest {
    private String preferWalletKey;
}
