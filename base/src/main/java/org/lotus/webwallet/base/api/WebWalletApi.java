package org.lotus.webwallet.base.api;

import org.lotus.webwallet.base.api.dto.*;
import org.lotus.webwallet.base.api.enums.SupportedCoins;

import java.math.BigDecimal;

/**
 * web wallet api
 *
 * @author : foy
 * @date : 2024/12/22:15:14
 *
 **/
public interface WebWalletApi {

    /**
     * ensure wallet for given account and password
     * if wallet exist return wallet summary info
     * if not exist create one
     * @param request wallet request
     * @return wallet info
     */
    WalletOpResult<EnsureWalletResult> ensureWallet(EnsureWalletRequest request);

    /**
     * get a public address for account
     * @param baseRequest wallet info
     * @return address
     */
    String getAddress(WalletBaseRequest baseRequest);

    /**
     * transfer give amount to given address
     * @param baseRequest request info
     * @param base58ToAddress address
     * @param amount amount
     * @return transfer result
     */
    WalletOpResult<TransferResult> transferToAddress(WalletBaseRequest baseRequest, String base58ToAddress,BigDecimal amount);

    /**
     * is given address are correctly for coin
     * @param base58ToAddress address
     * @param coin coin
     * @return true is address valid
     */
    boolean valid2Address(String base58ToAddress, SupportedCoins coin);

    /**
     * test if given coin support
     * @param coin coin
     * @return true if we can process
     */
    boolean supportCoin(SupportedCoins coin);
}
