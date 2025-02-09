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
     * get current coin net info
     * @param coin coin
     * @return net info
     */
    CoinNetInfo currentNetInfo(SupportedCoins coin);

    public static final String SUCCESS = "SUCCESS";
    /**
     * ensure wallet for given account and password
     * if wallet exist return wallet summary info
     * if not exist create one
     * @param request wallet request
     * @param eventListenerCallback wallet call back
     * @return wallet info
     */
    WalletOpResult<EnsureWalletResult> ensureWallet(EnsureWalletRequest request,WalletEventListenerCallback eventListenerCallback);

    /**
     * just ensure given wallet key is ready.
     * if wallet is not there, not create one,just info result.
     * @param request wallet key
     * @param eventListenerCallback wallet change call back
     * @return true if load ok
     */
    WalletOpResult<WalletBaseResult> loadWalletKey(WalletBaseRequest request,WalletEventListenerCallback eventListenerCallback);

    /**
     * get a public address for account
     * @param baseRequest wallet info
     * @return address
     */
    String getAddress(WalletBaseRequest baseRequest);

    /**
     * check if password is right for given wallet
     * @param request check request
     * @return password if password ok
     */
    WalletOpResult<Boolean> checkWalletPassword(WalletBaseRequest request);


    /**
     * change given password for wallet
     * @param request request
     * @return true wallet result
     */
    WalletOpResult<Boolean> changeWalletPassword(ChangeWalletPasswordRequest request);

    /**
     * transfer give amount to given address
     * @param baseRequest request info
     * @param base58ToAddress address
     * @param amount amount
     * @param base58ChangeAddress optional change address
     * @return transfer result
     *
     */
    WalletOpResult<TransferResult> transferToAddress(WalletBaseRequest baseRequest, String base58ToAddress,BigDecimal amount,String base58ChangeAddress);

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

    /**
     * @param coins current coins
     * current net info
     * @return net info
     */
     String netInfo(SupportedCoins coins);
}
