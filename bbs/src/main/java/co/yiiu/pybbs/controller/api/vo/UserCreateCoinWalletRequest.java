package co.yiiu.pybbs.controller.api.vo;

import lombok.Data;
import org.lotus.webwallet.base.api.enums.SupportedCoins;

/**
 * @author : foy
 * @date : 2025/1/3:13:51
 **/
@Data
public class UserCreateCoinWalletRequest {
    private String coinSymbol;
    private String encryptedOldPassword;
    private String encryptedPassword;
    private String pubIdxKey;
    private boolean saveEncryptedPasswordForThisWallet;
}
