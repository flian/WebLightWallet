package co.yiiu.pybbs.controller.api.vo;

import lombok.Data;

/**
 * @author : foy
 * @date : 2025/1/3:13:58
 **/
@Data
public class TransferCoinAmountRequest {
    private String coinSymbol;
    private String toUserName;
    private String toAddress;
    private double amount;
    private String encryptedPassword;
    private String pubIdxKey;
    private String uuid;
}
