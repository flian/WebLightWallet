package org.lotus.webwallet.infinitecoin.impl;

import com.google.infinitecoinj.core.DownloadListener;
import org.lotus.webwallet.base.api.WalletBlockChainDownloadProcessEventCallback;
import org.lotus.webwallet.base.api.enums.SupportedCoins;
import org.lotus.webwallet.base.api.enums.WalletBlockChainDownloadProcessType;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : foy
 * @date : 2025/1/8:09:15
 **/
public class IFCWalletDownloadListener extends DownloadListener {
    WalletBlockChainDownloadProcessEventCallback callback;
    SupportedCoins coin;
    String walletKey;

    public IFCWalletDownloadListener(SupportedCoins coins,String walletKey,WalletBlockChainDownloadProcessEventCallback callback) {
        this.coin = coins;
        this.walletKey = walletKey;
        this.callback = callback;
    }
    @Override
    protected void progress(double pct, int blocksSoFar, Date date) {
        super.progress(pct, blocksSoFar, date);
        Map<String,Object> eventParams = new HashMap<>();
        eventParams.put(WalletBlockChainDownloadProcessEventCallback.PCK_KEY,pct);
        eventParams.put(WalletBlockChainDownloadProcessEventCallback.BLOCKS_SO_FAR_KEY,blocksSoFar);
        eventParams.put(WalletBlockChainDownloadProcessEventCallback.LAST_BLOCK_DOWNLOAD_DATE_KEY,date);
        callback.onEvent(WalletBlockChainDownloadProcessType.progress,coin,walletKey,eventParams);
    }

    @Override
    protected void doneDownload() {
        super.doneDownload();
        Map<String,Object> eventParams = new HashMap<>();
        callback.onEvent(WalletBlockChainDownloadProcessType.doneDownload,coin,walletKey,eventParams);
    }
}