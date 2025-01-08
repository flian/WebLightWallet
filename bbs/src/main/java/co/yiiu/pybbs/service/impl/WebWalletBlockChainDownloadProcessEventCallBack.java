package co.yiiu.pybbs.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.lotus.webwallet.base.api.WalletBlockChainDownloadProcessEventCallback;
import org.lotus.webwallet.base.api.enums.SupportedCoins;
import org.lotus.webwallet.base.api.enums.WalletBlockChainDownloadProcessType;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author : foy
 * @date : 2025/1/8:10:43
 **/
@Slf4j
@Service
public class WebWalletBlockChainDownloadProcessEventCallBack implements WalletBlockChainDownloadProcessEventCallback {

    @Override
    public void onEvent(WalletBlockChainDownloadProcessType walletEvenType, SupportedCoins coin, String walletKey, Map<String, Object> eventParams) {
        if(walletEvenType == WalletBlockChainDownloadProcessType.doneDownload){
            log.info("walletKey:{},coin:{},wallet load don.",walletKey,coin.name());
        }
        if(walletEvenType == WalletBlockChainDownloadProcessType.progress){
            log.info("coin:{},wallet:{} loading process:{},lastBlockInfo:{},lastBlockDownLoadDate:{}"
                    ,coin.name(),walletKey,eventParams.get(WalletBlockChainDownloadProcessEventCallback.PCK_KEY)
                    ,eventParams.get(WalletBlockChainDownloadProcessEventCallback.LAST_BLOCK_DOWNLOAD_DATE_KEY)
                    ,eventParams.get(WalletBlockChainDownloadProcessEventCallback.LAST_BLOCK_DOWNLOAD_DATE_KEY));
        }
    }
}
