package org.lotus.webwallet.infinitecoin.impl;

import lombok.Getter;
import lombok.Setter;
import org.lotus.webwallet.base.api.dto.*;
import org.lotus.webwallet.base.api.enums.SupportedCoins;
import org.lotus.webwallet.base.config.WebWalletFileConfigProperties;
import org.lotus.webwallet.base.impl.BaseAbstractWebWallet;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @author : foy
 * @date : 2024/12/22:16:59
 **/
@Getter
@Setter
@Service(value = Constants.INFINITE_COIN)
public class IFCWebWallet extends BaseAbstractWebWallet {
    @Resource
    private WebWalletFileConfigProperties webWalletFileConfigProperties;

    @Override
    public WalletOpResult<EnsureWalletResult> ensureWallet(EnsureWalletRequest request) {
        return null;
    }

    @Override
    public String getAddress(WalletBaseRequest baseRequest) {
        return null;
    }

    @Override
    public WalletOpResult<TransferResult> transferToAddress(WalletBaseRequest baseRequest, String base58ToAddress, BigDecimal amount) {
        return null;
    }

    @Override
    public boolean valid2Address(String base58ToAddress, SupportedCoins coin) {
        return false;
    }

    @Override
    public boolean supportCoin(SupportedCoins coin) {
        return false;
    }
}
