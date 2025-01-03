package co.yiiu.pybbs.mapper;

import co.yiiu.pybbs.model.RsaPrivatePubKey;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author : foy
 * @date : 2025/1/2:11:42
 **/
public interface RsaPrivatePubKeyMapper extends BaseMapper<RsaPrivatePubKey> {
    RsaPrivatePubKey randomPickOneForUse();
    RsaPrivatePubKey selectByPubIdxKey(@Param("pubIdxKey") String pubIdxKey);
}
