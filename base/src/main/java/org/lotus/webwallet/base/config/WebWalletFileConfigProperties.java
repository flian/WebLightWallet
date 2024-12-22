package org.lotus.webwallet.base.config;

import org.lotus.webwallet.base.api.enums.SupportedCoins;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.File;

/**
 * @author : foy
 * @date : 2024/12/22:17:09
 **/
@Component
@ConfigurationProperties(prefix = "web.wallet.root.file")
public class WebWalletFileConfigProperties {
    private String path;

    private static String DEFAULT_PATH = "./";
    private static boolean inited = false;

    public String getCoinRootPath(SupportedCoins coin) {
        if (null == coin || coin.equals(SupportedCoins.DEFAULT_NOT_SUPPORT_COIN)) {
            throw new IllegalArgumentException("coin is null or not support coin.");
        }
        return getPath() + File.separator + coin.name();
    }

    protected String getPath() {
        if (ObjectUtils.isEmpty(path)) {
            if (!inited) {
                inited = true;
                DEFAULT_PATH = initDefaultPath();
            }
            return DEFAULT_PATH;
        }
        return path;
    }

    protected String initDefaultPath() {
        String os = System.getProperty("os.name").toLowerCase();
        if(os.contains("win")){
            return "D:/web_wallet";
        }else if(os.contains("nix") || os.contains("nux") || os.contains("aix")){
            return "/home/web_wallet";
        }else if(os.contains("mac")){
            return "~/web_wallet";
        }
        throw new RuntimeException("unknown system."+os);
    }
}
