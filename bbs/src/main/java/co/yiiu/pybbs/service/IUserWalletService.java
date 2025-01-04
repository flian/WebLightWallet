package co.yiiu.pybbs.service;


import co.yiiu.pybbs.model.User;
import co.yiiu.pybbs.model.UserWallet;
import co.yiiu.pybbs.service.vo.*;
import org.lotus.webwallet.base.api.enums.SupportedCoins;

import java.util.List;

/**
 * @author : foy
 * @date : 2025/1/2:20:50
 **/
public interface IUserWalletService {

    /**
     * top n user for supported coins
     * @param topN topN user
     * @return list
     */
    CoinRank listTopUsers(SupportedCoins coin,int topN);

    /**
     * generate rsa pub and private key for using.
     * @param count how many keys to generate and save in db
     * @return true means success
     */
    boolean genAndSavePrivateKeys(int count);

    /**
     * choose a rsa pubkey for front user use
     * @param preferKey which key perfer use. can be prefix or indx key
     * @return rsa pub key for use
     */
    RsaPubKeyInfoForFrontDto pickOneRsaPubKey(String preferKey);

    /**
     * query suer wallet info
     * @param username username
     * @param coins coin
     * @return wallet info
     */
    UserWallet selectUserWalletByUserAndCoin(String username, SupportedCoins coins);

    /**
     * init or change password for given user and wallet
     * @param user user for init wallet
     * @param requestDto wallet request
     * @return true if success
     */
    boolean initForUserWallet(User user, WalletKeyAndPasswordInfoInitRequestDto requestDto);

    /**
     * transfer coin from user to others
     * @param user user
     * @param transferCoinRequestDto transaction info
     * @return true if success
     */
    boolean transferCoin(User user,TransferCoinRequestDto transferCoinRequestDto);

    /**
     * change user wallet password
     * @param user user need change password
     * @param requestDto wallet info
     * @return true if change wallet success
     */
    boolean changeUserWalletPassword(User user, WalletResetPasswordRequestDto requestDto);

    /**
     * check user password for wallet
     * @param user user
     * @param requestDto wallet info
     * @return true if password is ok
     */
    boolean checkPasswordForWallet(User user,WalletKeyAndPasswordInfoInitRequestDto requestDto);
}
