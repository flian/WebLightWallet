package co.yiiu.pybbs.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author : foy
 * @date : 2025/1/2:11:33
 **/
@Getter
@Setter
public class RsaPrivatePubKey extends BaseEntity implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String privateKey;
    private String publicKey;
    private String idxKey;
}
