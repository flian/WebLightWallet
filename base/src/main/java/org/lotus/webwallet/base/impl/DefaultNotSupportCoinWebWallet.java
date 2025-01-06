package org.lotus.webwallet.base.impl;

import org.lotus.webwallet.base.api.dto.*;
import org.lotus.webwallet.base.api.enums.SupportedCoins;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * default not support exception impl
 *
 * @author : foy
 * @date : 2024/12/22:16:05
 **/

@Service(value = Constants.DEFAULT_NOT_SUPPORT_COIN)
public class DefaultNotSupportCoinWebWallet extends BaseAbstractWebWallet {
    protected static final CoinNetInfo NOT_SUPPORT_COIN = new CoinNetInfo(SupportedCoins.DEFAULT_NOT_SUPPORT_COIN, "", "NONE", "");

    @Override
    public CoinNetInfo currentNetInfo(SupportedCoins coin) {
        return NOT_SUPPORT_COIN;
    }

    @Override
    public WalletOpResult<EnsureWalletResult> ensureWallet(EnsureWalletRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WalletOpResult<WalletBaseResult> loadWalletKey(WalletBaseRequest baseRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAddress(WalletBaseRequest baseRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WalletOpResult<Boolean> checkWalletPassword(WalletBaseRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WalletOpResult<Boolean> changeWalletPassword(ChangeWalletPasswordRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WalletOpResult<TransferResult> transferToAddress(WalletBaseRequest baseRequest, String base58ToAddress, BigDecimal amount, String base58ChangeAddress) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean valid2Address(String base58ToAddress, SupportedCoins coin) {
        return false;
    }

    @Override
    public boolean supportCoin(SupportedCoins coin) {
        return true;
    }

    @Override
    public String netInfo(SupportedCoins coins) {
        throw new UnsupportedOperationException();
    }
}
