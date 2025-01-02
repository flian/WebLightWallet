package co.yiiu.pybbs.config;

import co.yiiu.pybbs.service.UserWalletService;
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
    private UserWalletService userWalletService;

    @Value("${spring.webwallet.coin.rsa.enabled:false}")
    private boolean initRsaKeys;

    @Value("${spring.webwallet.coin.rsa.initsize:100}")
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
