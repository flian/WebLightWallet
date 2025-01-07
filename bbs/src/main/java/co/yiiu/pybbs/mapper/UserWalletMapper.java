package co.yiiu.pybbs.mapper;


import co.yiiu.pybbs.model.User;
import co.yiiu.pybbs.model.UserWallet;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author : foy
 * @date : 2025/1/2:11:40
 **/
public interface UserWalletMapper extends BaseMapper<UserWallet> {
    UserWallet selectUserWalletByUserAndCoin(@Param("username") String username,@Param("coinSymbol") String coinSymbol);

    User selectWalletOwnerByWalletKey(@Param("walletKey") String walletKey,@Param("coinSymbol") String coinSymbol);
}
