package org.lotus.webwallet.base.api.dto;

import lombok.Data;
import org.lotus.webwallet.base.api.enums.SupportedCoins;

/**
 *
 * @author : foy
 * @date : 2024/12/22:15:21
 **/

@Data
public class WalletBaseRequest {
    private SupportedCoins coin;
    private String accountPrimaryKey;
    private String password;
}
