package co.yiiu.pybbs.controller.api.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : foy
 * @date : 2025/1/4:17:52
 **/
@Data
public class UserWalletInfo {
    private Integer id;
    private String username;
    private String coinSymbol;
    private String walletKey;
    private BigDecimal balance;
    private BigDecimal lockedAmount;
    private String primaryAddress;
}
