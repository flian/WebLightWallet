package co.yiiu.pybbs.service.vo;

import co.yiiu.pybbs.model.User;
import co.yiiu.pybbs.model.UserWallet;
import lombok.Data;
import org.lotus.webwallet.base.api.enums.SupportedCoins;

import java.util.List;

/**
 * @author : foy
 * @date : 2025/1/4:17:04
 **/
@Data
public class CoinRank {
    private SupportedCoins coin;
    private List<User> users;
    private List<UserWallet> wallets;
}
