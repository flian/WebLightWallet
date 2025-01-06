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
     * batter balance value for hum
     */
    private String balanceBetterShowValue;

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

    /**
     * if wallet is exist
     */
    private boolean walletExist;

    /**
     * if wallet is exist, if wallet is load and ready to work
     */
    private boolean loaded;
}
