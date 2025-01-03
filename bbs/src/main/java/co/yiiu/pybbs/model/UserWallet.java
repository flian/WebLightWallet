package co.yiiu.pybbs.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author : foy
 * @date : 2025/1/2:11:32
 **/
@Getter
@Setter
public class UserWallet implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String username;
    private String coinSymbol;
    private String walletKey;
    private BigDecimal balance;
    private BigDecimal lockedAmount;
    private String primaryAddress;
    private String encryptedPassword;
    private String pubIdxKey;
    private boolean savedWalletPassword;

    public double getAvailableAmount(){
        if(null != balance && null != lockedAmount){
            return balance.subtract(lockedAmount).setScale(2, RoundingMode.DOWN).doubleValue();
        }
        return 0.0;
    }
}
