package co.yiiu.pybbs.service.vo;

import co.yiiu.pybbs.model.User;
import lombok.Data;
import org.lotus.webwallet.base.api.enums.SupportedCoins;

/**
 * @author : foy
 * @date : 2025/1/3:14:07
 **/
@Data
public class TransferCoinRequestDto {
    private SupportedCoins coins;
    private User toUser;
    private String toAddress;
    private double amount;
    private String encryptedPassword;
    private String pubIdxKey;
}
