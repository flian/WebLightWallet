package org.lotus.webwallet.base.exceptions;

/**
 * @author : foy
 * @date : 2024/12/30:16:53
 **/
public class BizException extends  RuntimeException {
    public BizException(){
        super();
    }
    public BizException(String errorMsg){
        super(errorMsg);
    }
}
