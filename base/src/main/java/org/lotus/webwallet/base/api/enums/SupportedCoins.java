package org.lotus.webwallet.base.api.enums;

import java.math.BigInteger;

/**
 * @author : foy
 * @date : 2024/12/22:15:31
 **/
public enum SupportedCoins {
    INFINITE_COIN,
    DOGM_COIN,
    DEFAULT_NOT_SUPPORT_COIN;

    public static BigInteger dustAmount(SupportedCoins coin){
        if(INFINITE_COIN == coin){
            //current IFC dust min is 1000
            return BigInteger.valueOf(1000);
        }
        if(DOGM_COIN == coin){
            //dogm current dust is 1
            return BigInteger.valueOf(1);
        }
        return BigInteger.ZERO;
    }
}
