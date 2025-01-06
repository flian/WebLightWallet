package org.lotus.webwallet.base.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lotus.webwallet.base.api.enums.SupportedCoins;

/**
 * @author : foy
 * @date : 2025/1/6:21:40
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoinNetInfo {
    private SupportedCoins coins;
    private String coinDesc;
    private String currentNet;
    private String currentNetDesc;
}
