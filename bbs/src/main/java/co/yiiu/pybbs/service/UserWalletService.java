package co.yiiu.pybbs.service;

import co.yiiu.pybbs.model.RsaPrivatePubKey;
import co.yiiu.pybbs.service.vo.RsaPubKeyInfoForFrontDto;
import co.yiiu.pybbs.service.vo.WalletKeyAndPasswordInfoInitRequestDto;

import java.security.NoSuchAlgorithmException;

/**
 * @author : foy
 * @date : 2025/1/2:20:50
 **/
public interface UserWalletService {
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
     * init or change password for given user and wallet
     * @param userName user name
     * @param requestDto wallet request
     * @return true if success
     */
    boolean initOrChangePasswordForWallet(String userName, WalletKeyAndPasswordInfoInitRequestDto requestDto);
}
