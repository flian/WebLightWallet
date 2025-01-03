package co.yiiu.pybbs.config;

import co.yiiu.pybbs.service.IUserWalletService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


import javax.annotation.PostConstruct;
import javax.annotation.Resource;


/**
 * @author : foy
 * @date : 2025/1/2:22:02
 **/
@Configuration
@Slf4j
public class CoinInitConfig {
    @Resource
    private IUserWalletService userWalletService;

    @Value("${web.wallet.rsa.enabled:false}")
    private boolean initRsaKeys;

    @Value("${web.wallet.rsa.initSize:1000}")
    private int initSize;
    @PostConstruct
    public void initRsaKeys() {
        if(!initRsaKeys){
            log.info("disabled init RSA keys,skip insert keys.");
            return;
        }

        if(userWalletService.genAndSavePrivateKeys(initSize)){
            log.info("init RSA keys success with size:{}",initSize);
        }
    }
}
