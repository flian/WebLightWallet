package org.lotus.webwallet.base.api;

import org.lotus.webwallet.base.api.enums.SupportedCoins;
import org.lotus.webwallet.base.api.enums.WalletEvenType;

import java.util.Map;

/**
 * @author : foy
 * @date : 2025/1/7:18:01
 **/
public interface WalletEventListenerCallback {
     String WALLET_KEY_IN_MAP = "wallet";
     String TRANSACTION_KEY_IN_MAP = "tx";
    void onEvent(WalletEvenType walletEvenType, SupportedCoins coin,String walletKey, Map<String,Object> eventParams);
}
