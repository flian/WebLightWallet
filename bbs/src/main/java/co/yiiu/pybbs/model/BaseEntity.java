package co.yiiu.pybbs.model;

import com.baomidou.mybatisplus.annotation.TableLogic;

/**
 * @author : foy
 * @date : 2025/1/8:14:37
 **/
public class BaseEntity {
    @TableLogic
    private Integer isDeleted;

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}
