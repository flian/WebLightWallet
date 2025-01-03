package co.yiiu.pybbs.controller.api.vo;

import lombok.Data;
import org.lotus.webwallet.base.api.enums.SupportedCoins;

/**
 * @author : foy
 * @date : 2025/1/3:12:53
 **/
@Data
public class UserCoinInfoResult {
    private SupportedCoins coin;
    private String coinName;
    private String primaryAddress;
    private Double availableAmount;
    private boolean active;
}
