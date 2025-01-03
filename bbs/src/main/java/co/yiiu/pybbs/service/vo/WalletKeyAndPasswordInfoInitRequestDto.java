package co.yiiu.pybbs.service.vo;

import lombok.Data;
import org.lotus.webwallet.base.api.enums.SupportedCoins;

/**
 * @author : foy
 * @date : 2025/1/2:20:55
 **/
@Data
public class WalletKeyAndPasswordInfoInitRequestDto {
    private SupportedCoins coinSymbol;
    private String encryptedPassword;
    private String pubIdxKey;
    private boolean saveEncryptedPasswordForThisWallet;
}
