package org.lotus.webwallet.base.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.lotus.webwallet.base.api.WebWalletApi;
import org.lotus.webwallet.base.api.dto.*;
import org.lotus.webwallet.base.api.enums.SupportedCoins;
import org.lotus.webwallet.base.exceptions.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;


import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;

/**
 * default web wallet strategy impl
 *
 * @author : foy
 * @date : 2024/12/22:16:12
 **/

@Getter
@Slf4j
@Service(value = Constants.DEFAULT_WALLET_STRATEGY_NAME)
public class WebWalletStrategy extends BaseAbstractWebWallet {

    private Map<String, WebWalletApi> webWalletsMap;

    @Resource(name = Constants.DEFAULT_NOT_SUPPORT_COIN)
    private WebWalletApi notFoundDefaultWebWallet;

    protected WebWalletApi getWebWalletByCoin(String coin) {
        log.info("try find coin web wallet implementing,coin:{}", coin);
        if (webWalletsMap.containsKey(coin)) {
            log.info("find one web wallet implementing:{}", webWalletsMap.get(coin).getClass());
            return webWalletsMap.get(coin);
        }

        log.info("can't find wallet implement,return default one:{}", notFoundDefaultWebWallet.getClass());
        return notFoundDefaultWebWallet;
    }

    @Override
    public WalletOpResult<EnsureWalletResult> ensureWallet(EnsureWalletRequest request) {
        if (null == request.getCoin()) {
            throw new IllegalArgumentException("coin is required");
        }
        if (ObjectUtils.isEmpty(request.getAccountPrimaryKey())) {
            throw new IllegalArgumentException("accountPrimaryKey is required");
        }
        if (ObjectUtils.isEmpty(request.getPassword())) {
            log.warn("wallet password is null.");
        }
        WebWalletApi webWalletApi = getWebWalletByCoin(request.getCoin().name());

        return webWalletApi.ensureWallet(request);
    }

    @Override
    public WalletOpResult<WalletBaseResult> loadWalletKey(WalletBaseRequest request) {
        if (null == request.getCoin()) {
            throw new IllegalArgumentException("coin is required");
        }
        if (ObjectUtils.isEmpty(request.getAccountPrimaryKey())) {
            throw new IllegalArgumentException("accountPrimaryKey is required");
        }
        WebWalletApi webWalletApi = getWebWalletByCoin(request.getCoin().name());

        return webWalletApi.loadWalletKey(request);
    }

    @Override
    public String getAddress(WalletBaseRequest baseRequest) {
        if (null == baseRequest.getCoin()) {
            throw new IllegalArgumentException("coin is required");
        }
        if (ObjectUtils.isEmpty(baseRequest.getAccountPrimaryKey())) {
            throw new IllegalArgumentException("accountPrimaryKey is required");
        }
        WebWalletApi webWalletApi = getWebWalletByCoin(baseRequest.getCoin().name());
        return webWalletApi.getAddress(baseRequest);
    }

    @Override
    public WalletOpResult<Boolean> checkWalletPassword(WalletBaseRequest request) {
        if (null == request.getCoin()) {
            throw new IllegalArgumentException("coin is required");
        }
        if (ObjectUtils.isEmpty(request.getAccountPrimaryKey())) {
            throw new IllegalArgumentException("accountPrimaryKey is required");
        }
        if(ObjectUtils.isEmpty(request.getPassword())){
            throw new IllegalArgumentException("password is required");
        }
        WebWalletApi webWalletApi = getWebWalletByCoin(request.getCoin().name());
        return webWalletApi.checkWalletPassword(request);
    }

    @Override
    public WalletOpResult<Boolean> changeWalletPassword(ChangeWalletPasswordRequest request) {
        if (null == request.getCoin()) {
            throw new IllegalArgumentException("coin is required");
        }
        if (ObjectUtils.isEmpty(request.getAccountPrimaryKey())) {
            throw new IllegalArgumentException("accountPrimaryKey is required");
        }
        if(ObjectUtils.isEmpty(request.getNewPassword())){
            throw new IllegalArgumentException("new password is required");
        }
        WebWalletApi webWalletApi = getWebWalletByCoin(request.getCoin().name());
        return webWalletApi.changeWalletPassword(request);
    }

    @Override
    public WalletOpResult<TransferResult> transferToAddress(WalletBaseRequest baseRequest, String base58ToAddress, BigDecimal amount,String base58ChangeAddress) {
        if (null == baseRequest.getCoin()) {
            throw new IllegalArgumentException("coin is required");
        }
        if (ObjectUtils.isEmpty(baseRequest.getAccountPrimaryKey())) {
            throw new IllegalArgumentException("accountPrimaryKey is required");
        }
        if (ObjectUtils.isEmpty(base58ToAddress) || !valid2Address(base58ToAddress, baseRequest.getCoin())) {
            log.error("address is empty or invalid.address:{},coin:{}", base58ToAddress, baseRequest.getCoin().name());
            throw new IllegalArgumentException("address is empty or invalid.");
        }
        if (null == amount || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("amount invalid for transfer,coin:{},address:{},amount:{}", baseRequest.getCoin().name(), base58ToAddress, amount.doubleValue());
            throw new IllegalArgumentException("amount is empty or invalid.");
        }
        WebWalletApi webWalletApi = getWebWalletByCoin(baseRequest.getCoin().name());
        WalletOpResult<Boolean> checkResult = webWalletApi.checkWalletPassword(baseRequest);
        if(!(checkResult.isOk()&&checkResult.getData())){
           //password is not right.
            throw  new BizException("check wallet password fail.");
        }
        return webWalletApi.transferToAddress(baseRequest, base58ToAddress, amount,base58ChangeAddress);
    }

    @Override
    public boolean valid2Address(String base58ToAddress, SupportedCoins coin) {
        if (null == coin) {
            throw new IllegalArgumentException("coin is required");
        }
        WebWalletApi webWalletApi = getWebWalletByCoin(coin.name());
        return webWalletApi.valid2Address(base58ToAddress, coin);
    }

    @Override
    public boolean supportCoin(SupportedCoins coin) {
        if (null == coin) {
            throw new IllegalArgumentException("coin is required");
        }
        WebWalletApi webWalletApi = getWebWalletByCoin(coin.name());
        return webWalletApi.supportCoin(coin);
    }

    @Override
    public String netInfo(SupportedCoins coins) {
        return getWebWalletByCoin(coins.name()).netInfo(coins);
    }

    @Autowired
    public void setWebWalletsMap(Map<String, WebWalletApi> maps) {
        this.webWalletsMap = maps;
    }

}
