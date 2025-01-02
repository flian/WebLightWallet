package co.yiiu.pybbs.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author : foy
 * @date : 2025/1/2:11:32
 **/
@Getter
@Setter
public class UserCoinAddress implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String label;
    private String walletKey;
    private String address;
}
