package org.lotus.webwallet.infinitecoin.impl;


import com.google.infinitecoinj.core.ECKey;
import com.google.infinitecoinj.core.Transaction;
import com.google.infinitecoinj.core.Wallet;
import com.google.infinitecoinj.core.WalletEventListener;
import com.google.infinitecoinj.script.Script;
import org.lotus.webwallet.base.api.WalletEventListenerCallback;
import org.lotus.webwallet.base.api.enums.SupportedCoins;
import org.lotus.webwallet.base.api.enums.WalletEvenType;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : foy
 * @date : 2025/1/7:19:06
 **/
public class IFCWalletEventListener implements WalletEventListener {
    WalletEventListenerCallback callback;
    SupportedCoins coin;
    String walletKey;

    public IFCWalletEventListener(SupportedCoins coin,String walletKey,WalletEventListenerCallback callback){
        this.coin = coin;
        this.walletKey = walletKey;
        this.callback = callback;
    }

    @Override
    public void onCoinsReceived(Wallet wallet, Transaction tx, BigInteger prevBalance, BigInteger newBalance) {
        if(null != callback){
            Map<String,Object> params = new HashMap<>();
            params.put(WalletEventListenerCallback.WALLET_KEY_IN_MAP,wallet);
            params.put(WalletEventListenerCallback.TRANSACTION_KEY_IN_MAP,tx);
            params.put("prevBalance",prevBalance);
            params.put("newBalance",newBalance);
            callback.onEvent(WalletEvenType.onCoinsReceived,coin,walletKey,params);
        }
    }

    @Override
    public void onCoinsSent(Wallet wallet, Transaction tx, BigInteger prevBalance, BigInteger newBalance) {
        if(null != callback){
            Map<String,Object> params = new HashMap<>();
            params.put(WalletEventListenerCallback.WALLET_KEY_IN_MAP,wallet);
            params.put(WalletEventListenerCallback.TRANSACTION_KEY_IN_MAP,tx);
            params.put("prevBalance",prevBalance);
            params.put("newBalance",newBalance);
            callback.onEvent(WalletEvenType.onCoinsSent,coin,walletKey,params);
        }
    }

    @Override
    public void onReorganize(Wallet wallet) {

    }

    @Override
    public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
        if(null != callback){
            Map<String,Object> params = new HashMap<>();
            params.put(WalletEventListenerCallback.WALLET_KEY_IN_MAP,wallet);
            params.put(WalletEventListenerCallback.TRANSACTION_KEY_IN_MAP,tx);
            callback.onEvent(WalletEvenType.onTransactionConfidenceChanged,coin,walletKey,params);
        }
    }

    @Override
    public void onWalletChanged(Wallet wallet) {

    }

    @Override
    public void onKeysAdded(Wallet wallet, List<ECKey> keys) {

    }

    @Override
    public void onScriptsAdded(Wallet wallet, List<Script> scripts) {

    }
}
