package org.lotus.webwallet.base.api.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : foy
 * @date : 2024/12/22:15:38
 **/
@Data
public class WalletBaseResult {
    /**
     * major address accept blockchain amount transfer
     */
    private String base58Address;
    /**
     * total balance for given wallet
     */
    private BigDecimal balance;

    /**
     * wallet primary key
     */
    private String walletKey;

    /**
     * where wallet is save
     */
    private String walletSavePlace;

    /**
     * wallet summary info
     */
    private String walletInfo;
}
