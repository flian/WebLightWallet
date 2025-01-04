package co.yiiu.pybbs.controller.api;


import co.yiiu.pybbs.controller.api.vo.TransferCoinAmountRequest;
import co.yiiu.pybbs.controller.api.vo.UserCoinInfoResult;
import co.yiiu.pybbs.controller.api.vo.UserCreateCoinWalletRequest;
import co.yiiu.pybbs.model.User;
import co.yiiu.pybbs.model.UserWallet;
import co.yiiu.pybbs.service.IUserService;
import co.yiiu.pybbs.service.IUserWalletService;
import co.yiiu.pybbs.service.vo.RsaPubKeyInfoForFrontDto;
import co.yiiu.pybbs.service.vo.TransferCoinRequestDto;
import co.yiiu.pybbs.service.vo.WalletKeyAndPasswordInfoInitRequestDto;
import co.yiiu.pybbs.service.vo.WalletResetPasswordRequestDto;
import co.yiiu.pybbs.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.lotus.webwallet.base.api.enums.SupportedCoins;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : foy
 * @date : 2025/1/3:12:44
 **/

@Slf4j
@RestController
@RequestMapping("/api/coin")
public class CoinApiController extends BaseApiController {
    @Resource
    private IUserWalletService userWalletService;

    @Resource
    private IUserService userService;

    protected List<UserCoinInfoResult> defaultCoinsStatus(){
        List<UserCoinInfoResult> resultList = new ArrayList<>();
        UserCoinInfoResult ifc = new UserCoinInfoResult();
        ifc.setCoin(SupportedCoins.INFINITE_COIN);
        ifc.setCoinName("IFC");
        ifc.setActive(false);
        resultList.add(ifc);
        return resultList;
    }

    @GetMapping("/public/key/rsa")
    public Result getPubKeyForEncrypt(@RequestParam(required = false,name = "preferKey") String preferKey){
        RsaPubKeyInfoForFrontDto rsaKey = userWalletService.pickOneRsaPubKey(preferKey);
        if(rsaKey != null){
            return success(rsaKey);
        }
        return error("can't get one pub key!");
    }

    @GetMapping("/{username}/coins")
    public Result listUserCoinInfo(@PathVariable(name = "username") String username){
        // 查询用户个人信息
        User user = userService.selectByUsername(username);
        List<UserCoinInfoResult> coinList = defaultCoinsStatus();
        coinList.forEach(coin->{
            UserWallet userWallet = userWalletService.selectUserWalletByUserAndCoin(user.getUsername(),coin.getCoin());
            if(null != userWallet){
                coin.setActive(true);
                coin.setPrimaryAddress(userWallet.getPrimaryAddress());
                coin.setAvailableAmount(userWallet.getAvailableAmount());
            }
        });
        return success(coinList);
    }

    @PostMapping("{username}/init/coin")
    public Result initCoinForUser(@PathVariable(name = "username") String username
            ,@RequestBody UserCreateCoinWalletRequest userCreateCoinWalletRequest){
        User me = getApiUser();
        if(null == me || !me.getUsername().equals(username)){
            log.error("error, invalid user request...");
            return error("invalid request.");
        }
        //init wallet case
        if(ObjectUtils.isEmpty(userCreateCoinWalletRequest.getEncryptedOldPassword())){
            WalletKeyAndPasswordInfoInitRequestDto requestDto = new WalletKeyAndPasswordInfoInitRequestDto();
            requestDto.setCoinSymbol(SupportedCoins.valueOf(userCreateCoinWalletRequest.getCoinSymbol()));
            requestDto.setPubIdxKey(userCreateCoinWalletRequest.getPubIdxKey());
            requestDto.setEncryptedPassword(userCreateCoinWalletRequest.getEncryptedPassword());
            requestDto.setSaveEncryptedPasswordForThisWallet(userCreateCoinWalletRequest.isSaveEncryptedPasswordForThisWallet());
            if(userWalletService.initForUserWallet(me,requestDto)){
                return success();
            }
        }else{
            //change password case
            WalletKeyAndPasswordInfoInitRequestDto checkRequest = new WalletKeyAndPasswordInfoInitRequestDto();
            checkRequest.setCoinSymbol(SupportedCoins.valueOf(userCreateCoinWalletRequest.getCoinSymbol()));
            checkRequest.setPubIdxKey(userCreateCoinWalletRequest.getPubIdxKey());
            checkRequest.setEncryptedPassword(userCreateCoinWalletRequest.getEncryptedOldPassword());
            if(userWalletService.checkPasswordForWallet(me,checkRequest)){
                WalletResetPasswordRequestDto resetPasswordRequestDto =new WalletResetPasswordRequestDto();
                resetPasswordRequestDto.setCoinSymbol(userCreateCoinWalletRequest.getCoinSymbol());
                resetPasswordRequestDto.setEncryptedOldPassword(userCreateCoinWalletRequest.getEncryptedOldPassword());
                resetPasswordRequestDto.setEncryptedNewPassword(userCreateCoinWalletRequest.getEncryptedPassword());
                resetPasswordRequestDto.setPubIdxKey(userCreateCoinWalletRequest.getPubIdxKey());
                resetPasswordRequestDto.setSaveEncryptedPasswordForThisWallet(userCreateCoinWalletRequest.isSaveEncryptedPasswordForThisWallet());
                return userWalletService.changeUserWalletPassword(me,resetPasswordRequestDto)?success():error("change password fail.");
            }else{
                return error("password is not right,u forget password?");
            }
        }

        return error("not result.");
    }

    @PostMapping("{username}/{coin}/transfer")
    public Result transferCoin(@PathVariable(name = "username") String username,
                              @PathVariable(name = "coin") String coin,
                              @RequestBody TransferCoinAmountRequest transferCoinAmountRequest){
        User me = getApiUser();
        if(null == me || !me.getUsername().equals(username)){
            log.error("error, invalid user request...");
            return error("invalid request.");
        }

        if(!coin.equals(transferCoinAmountRequest.getCoinSymbol())){
            log.error("invalid coin type.");
            return error("invalid coin type.");
        }
        SupportedCoins currentCoin = SupportedCoins.valueOf(transferCoinAmountRequest.getCoinSymbol());
        UserWallet userWallet = userWalletService.selectUserWalletByUserAndCoin(me.getUsername(),currentCoin);
        if(userWallet.getAvailableAmount() < transferCoinAmountRequest.getAmount()){
            return error("余额不足。");
        }
        TransferCoinRequestDto toRequestDto = new TransferCoinRequestDto();
        toRequestDto.setCoins(currentCoin);
        User toUser = userService.selectByUsername(transferCoinAmountRequest.getToUserName());
        toRequestDto.setToUser(toUser);
        toRequestDto.setToAddress(transferCoinAmountRequest.getToAddress());
        toRequestDto.setAmount(transferCoinAmountRequest.getAmount());
        toRequestDto.setPubIdxKey(transferCoinAmountRequest.getPubIdxKey());
        toRequestDto.setEncryptedPassword(transferCoinAmountRequest.getEncryptedPassword());
        if(userWalletService.transferCoin(me,toRequestDto)){
            return success();
        }
        return error("not result.");
    }
}
