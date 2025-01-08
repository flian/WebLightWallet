package org.lotus.webwallet.base.api;

import org.lotus.webwallet.base.api.enums.SupportedCoins;
import org.lotus.webwallet.base.api.enums.WalletBlockChainDownloadProcessType;


import java.util.Map;

/**
 * @author : foy
 * @date : 2025/1/8:09:03
 **/
public interface WalletBlockChainDownloadProcessEventCallback {
    String PCK_KEY = "pct";
    String BLOCKS_SO_FAR_KEY = "blocksSoFar";
    String LAST_BLOCK_DOWNLOAD_DATE_KEY = "lastBlockDownLoadDate";

    void onEvent(WalletBlockChainDownloadProcessType walletEvenType, SupportedCoins coin, String walletKey, Map<String,Object> eventParams);
}
