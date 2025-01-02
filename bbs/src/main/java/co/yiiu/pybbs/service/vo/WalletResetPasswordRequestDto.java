package co.yiiu.pybbs.service.vo;

import lombok.Data;

/**
 * @author : foy
 * @date : 2025/1/2:20:58
 **/
@Data
public class WalletResetPasswordRequestDto {
    private String walletKey;
    private String coinSymbol;
    private String encryptedOldPassword;
    private String encryptedNewPassword;
    private String pubIdxKey;
    private boolean saveEncryptedPasswordForThisWallet;
}
