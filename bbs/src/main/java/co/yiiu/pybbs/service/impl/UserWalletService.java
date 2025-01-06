package co.yiiu.pybbs.service.impl;

import co.yiiu.pybbs.mapper.RsaPrivatePubKeyMapper;
import co.yiiu.pybbs.mapper.UserWalletMapper;
import co.yiiu.pybbs.model.RsaPrivatePubKey;
import co.yiiu.pybbs.model.User;
import co.yiiu.pybbs.model.UserWallet;
import co.yiiu.pybbs.service.IUserService;
import co.yiiu.pybbs.service.IUserWalletService;
import co.yiiu.pybbs.service.vo.*;
import co.yiiu.pybbs.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.lotus.webwallet.base.api.dto.*;
import org.lotus.webwallet.base.api.enums.SupportedCoins;
import org.lotus.webwallet.base.impl.WebWalletStrategy;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author : foy
 * @date : 2025/1/2:21:36
 **/


@Slf4j
@Service
public class UserWalletService implements IUserWalletService, InitializingBean {
    protected static final String WALLET_ENCRYPTED_METHOD_KEY = "RSA";
    protected static final int DEFAULT_RSA_KEY_SIZE = 2048;

    private static KeyPairGenerator RSA_GENERATEOR = null;

    @Resource
    private RsaPrivatePubKeyMapper rsaPrivatePubKeyMapper;

    @Resource
    private UserWalletMapper userWalletMapper;

    @Resource
    private WebWalletStrategy webWalletStrategy;

    @Resource
    private IUserService userService;

    @Value("${web.wallet.rsa.defaultMaxSize:1000}")
    private int maxKeySize;

    @Override
    public CoinRank listTopUsers(SupportedCoins coin, int topN) {
        int limit = 100;
        if(topN<100){
            limit = topN;
        }
        QueryWrapper<UserWallet> wrapper = new QueryWrapper<>();
        wrapper.eq("coin_symbol",coin.name());
        wrapper.orderByDesc("balance").last("limit " + limit);
        List<UserWallet> userWallets =  userWalletMapper.selectList(wrapper);
        List<String> usernames = userWallets.stream().map(UserWallet::getUsername).collect(Collectors.toList());
        List<User> users = new ArrayList<>();
        List<User> list  = userService.listByUserNames(usernames);
        for(String u:usernames){
            list.stream().filter(uu -> uu.getUsername().equals(u)).findFirst().ifPresent(users::add);
        }
        CoinRank result = new CoinRank();
        result.setCoin(coin);
        result.setUsers(users);
        result.setWallets(userWallets);
        return result;
    }

    @Override
    public boolean genAndSavePrivateKeys(int count) {
        try {
            if(count > 0){
                if(rsaPrivatePubKeyMapper.selectCount(new QueryWrapper<>())>=maxKeySize){
                    log.info("reach max RSA key size,skip gen keys.");
                    return true;
                }
                List<KeyPair> needSave = new ArrayList<>(count);
                for(int i=0;i<count;i++){
                    needSave.add(generateRSAKeyPair(DEFAULT_RSA_KEY_SIZE));
                }
                needSave.forEach(keyPair -> {
                    RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
                    // 得到私钥
                    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
                    // 得到公钥
                    String publicKeyString = new String(Base64.encodeBase64(publicKey.getEncoded()));
                    // 得到私钥字符串
                    String privateKeyString = new String(Base64.encodeBase64((privateKey.getEncoded())));
                    RsaPrivatePubKey rsaPrivatePubKey = new RsaPrivatePubKey();
                    rsaPrivatePubKey.setPrivateKey(privateKeyString);
                    rsaPrivatePubKey.setPublicKey(publicKeyString);
                    rsaPrivatePubKey.setIdxKey(StringUtil.randUUidStr());
                    rsaPrivatePubKeyMapper.insert(rsaPrivatePubKey);
                });
                return true;
            }
        }catch (Exception e){
        log.error("error gen and save rsa keys.",e);
        }

        return false;
    }

    @Override
    public RsaPubKeyInfoForFrontDto pickOneRsaPubKey(String preferKey) {
        RsaPrivatePubKey one = rsaPrivatePubKeyMapper.randomPickOneForUse();
        RsaPubKeyInfoForFrontDto result = new RsaPubKeyInfoForFrontDto();
        result.setIdxKey(one.getIdxKey());
        result.setPublicKey(one.getPublicKey());
        return result;
    }

    @Override
    public UserWallet selectUserWalletByUserAndCoin(String username, SupportedCoins coins) {
        return userWalletMapper.selectUserWalletByUserAndCoin(username,coins.name());
    }



    @Override
    public boolean initForUserWallet(User user, WalletKeyAndPasswordInfoInitRequestDto requestDto) {
        UserWallet userWallet = userWalletMapper.selectUserWalletByUserAndCoin(user.getUsername(),requestDto.getCoinSymbol().name());
        if(null != userWallet){
            log.error("user already have  wallet for coin.user:{},coinSymbol:{},walletKey:{}",
                    user.getUsername(),requestDto.getCoinSymbol(),userWallet.getWalletKey());
            return false;
        }
        RsaPrivatePubKey key = rsaPrivatePubKeyMapper.selectByPubIdxKey(requestDto.getPubIdxKey());
        try {
            String password = deCryptText(requestDto.getEncryptedPassword(),key.getPrivateKey());
            String walletKey = genWalletKeyForUser(user, requestDto.getCoinSymbol().name());
            userWallet = new UserWallet();
            userWallet.setUsername(user.getUsername());
            userWallet.setCoinSymbol(requestDto.getCoinSymbol().name());
            userWallet.setWalletKey(walletKey);
            if(requestDto.isSaveEncryptedPasswordForThisWallet()){
                userWallet.setEncryptedPassword(requestDto.getEncryptedPassword());
                userWallet.setPubIdxKey(requestDto.getPubIdxKey());
                userWallet.setSavedWalletPassword(true);
            }
            userWallet.setBalance(BigDecimal.ZERO);
            userWallet.setLockedAmount(BigDecimal.ZERO);

            //try create a wallet
            EnsureWalletRequest ensureWalletRequest = new EnsureWalletRequest();
            ensureWalletRequest.setAccountPrimaryKey(walletKey);
            ensureWalletRequest.setCoin(SupportedCoins.valueOf(requestDto.getCoinSymbol().name()));
            ensureWalletRequest.setPassword(password);
            WalletOpResult<EnsureWalletResult> walletInfo = webWalletStrategy.ensureWallet(ensureWalletRequest);
            if(walletInfo.isOk()){
                userWallet.setWalletKey(walletInfo.getData().getWalletKey());
                userWallet.setPrimaryAddress(walletInfo.getData().getBase58Address());
                userWalletMapper.insert(userWallet);
                return true;
            }
        } catch (Exception e) {
            log.error("error create user wallet.user:{},coinSymbol:{}",user.getUsername(),requestDto.getCoinSymbol(),e);
        }

        return false;
    }

    @Override
    public boolean refreshCoinBalance(User user, SupportedCoins coin) {
        UserWallet userWallet = userWalletMapper.selectUserWalletByUserAndCoin(user.getUsername(),coin.name());
        if(null != userWallet){
            EnsureWalletRequest request = new EnsureWalletRequest();
            request.setCoin(coin);
            request.setAccountPrimaryKey(userWallet.getWalletKey());
            WalletOpResult<EnsureWalletResult> result = webWalletStrategy.ensureWallet(request);
            if(result.isOk()){
                userWallet.setBalance(result.getData().getBalance());
                userWalletMapper.updateById(userWallet);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean transferCoin(User user, TransferCoinRequestDto transferCoinRequestDto) {
        RsaPrivatePubKey rsaKey = rsaPrivatePubKeyMapper.selectByPubIdxKey(transferCoinRequestDto.getPubIdxKey());
        SupportedCoins currentCoin = transferCoinRequestDto.getCoins();
        try {
            String password = deCryptText(transferCoinRequestDto.getEncryptedPassword(),rsaKey.getPrivateKey());
            String toAddress = transferCoinRequestDto.getToAddress();
            if(ObjectUtils.isEmpty(toAddress)){
                UserWallet userWallet = userWalletMapper.selectUserWalletByUserAndCoin(transferCoinRequestDto.getToUser().getUsername(),currentCoin.name());
                toAddress = userWallet.getPrimaryAddress();
            }
            UserWallet fromWallet = userWalletMapper.selectUserWalletByUserAndCoin(user.getUsername(),transferCoinRequestDto.getCoins().name());
            WalletBaseRequest baseRequest = new WalletBaseRequest();
            baseRequest.setAccountPrimaryKey(fromWallet.getWalletKey());
            baseRequest.setCoin(currentCoin);
            baseRequest.setPassword(password);
            WalletOpResult<TransferResult> transferResult = webWalletStrategy.transferToAddress(baseRequest,toAddress,BigDecimal.valueOf(transferCoinRequestDto.getAmount()),fromWallet.getPrimaryAddress());
            if(transferResult.isOk()){
                return true;
            }
        } catch (Exception e) {
           log.error("error transfer coin.");
        }
        return false;
    }

    protected String genWalletKeyForUser(User user,String coinSymbol){
        return String.format("%s_%s_%s",Base64.encodeBase64String(user.getUsername().getBytes()),
                coinSymbol,StringUtil.randUUidStr());
    }

    @Override
    public boolean changeUserWalletPassword(User user, WalletResetPasswordRequestDto requestDto) {
        UserWallet userWallet = userWalletMapper.selectUserWalletByUserAndCoin(user.getUsername(),requestDto.getCoinSymbol());
        if(null == userWallet){
            log.error("user wallet is not created.user:{},coin:{}",user.getUsername(),requestDto.getCoinSymbol());
            return false;
        }
        try {
            RsaPrivatePubKey rsaKey = rsaPrivatePubKeyMapper.selectByPubIdxKey(requestDto.getPubIdxKey());
            String decryptedOldPassword = deCryptText(requestDto.getEncryptedOldPassword(),rsaKey.getPrivateKey());
            String decryptedNewPassword = deCryptText(requestDto.getEncryptedNewPassword(),rsaKey.getPrivateKey());
            ChangeWalletPasswordRequest request = new ChangeWalletPasswordRequest();
            request.setPassword(decryptedOldPassword);
            request.setNewPassword(decryptedNewPassword);
            request.setCoin(SupportedCoins.valueOf(requestDto.getCoinSymbol()));
            request.setAccountPrimaryKey(userWallet.getWalletKey());
            WalletOpResult<Boolean>  requestResult = webWalletStrategy.changeWalletPassword(request);
            boolean result =  requestResult.isOk()&& requestResult.getData();
            if(result){
                if(requestDto.isSaveEncryptedPasswordForThisWallet()){
                    //update save password info
                    userWallet.setPubIdxKey(requestDto.getPubIdxKey());
                    userWallet.setSavedWalletPassword(true);
                    userWallet.setEncryptedPassword(requestDto.getEncryptedNewPassword());
                }else {
                    //just update flag
                    userWallet.setSavedWalletPassword(false);
                }
                userWalletMapper.updateById(userWallet);
            }
            return result;
        }catch (Exception e){
            log.error("change password fail.",e);
        }

        return false;
    }

    @Override
    public boolean checkPasswordForWallet(User user, WalletKeyAndPasswordInfoInitRequestDto requestDto) {
        UserWallet userWallet = userWalletMapper.selectUserWalletByUserAndCoin(user.getUsername(),requestDto.getCoinSymbol().name());
        if(null == userWallet){
            log.error("user wallet is not created.user:{},coin:{}",user.getUsername(),requestDto.getCoinSymbol());
            return false;
        }
        try {
            RsaPrivatePubKey rsaKey = rsaPrivatePubKeyMapper.selectByPubIdxKey(requestDto.getPubIdxKey());
            String decryptedPassword = deCryptText(requestDto.getEncryptedPassword(),rsaKey.getPrivateKey());
            String walletKey = userWallet.getWalletKey();
            WalletBaseRequest request = new WalletBaseRequest();
            request.setCoin(SupportedCoins.valueOf(requestDto.getCoinSymbol().name()));
            request.setPassword(decryptedPassword);
            request.setAccountPrimaryKey(walletKey);
            WalletOpResult<Boolean> result = webWalletStrategy.checkWalletPassword(request);
            return result.isOk()&& result.getData();
        }catch (Exception e){
            log.error("check password fail.",e);
        }
        return false;
    }

    protected String deCryptText(String encryptedText,String privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        /// 64位解码加密后的字符串
        byte[] inputByte = Base64.decodeBase64(encryptedText.getBytes(StandardCharsets.UTF_8));
        // base64编码的私钥
        byte[] decoded = Base64.decodeBase64(privateKey);
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance(WALLET_ENCRYPTED_METHOD_KEY).generatePrivate(new PKCS8EncodedKeySpec(decoded));
        // RSA解密
        Cipher cipher = Cipher.getInstance(WALLET_ENCRYPTED_METHOD_KEY);
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        return new String(cipher.doFinal(inputByte));
    }

    protected String enCryptText(String text,String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // base64编码的公钥
        byte[] decoded = Base64.decodeBase64(publicKey);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance(WALLET_ENCRYPTED_METHOD_KEY).generatePublic(new X509EncodedKeySpec(decoded));
        // RSA加密
        Cipher cipher = Cipher.getInstance(WALLET_ENCRYPTED_METHOD_KEY);
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return Base64.encodeBase64String(cipher.doFinal(text.getBytes(StandardCharsets.UTF_8)));
    }

    protected KeyPair generateRSAKeyPair(int keySize) throws NoSuchAlgorithmException {
        if(null == RSA_GENERATEOR){
            synchronized (UserWalletService.class){
                RSA_GENERATEOR = KeyPairGenerator.getInstance(WALLET_ENCRYPTED_METHOD_KEY);
                RSA_GENERATEOR.initialize(keySize);
            }
        }
        return RSA_GENERATEOR.generateKeyPair();
    }


    protected void loadWallet(){
        //ensure wallet load
        QueryWrapper<UserWallet> queryWrapper = new QueryWrapper<>();
        int totalCount = userWalletMapper.selectCount(queryWrapper);
        int pageSize = 1000;
        int totalPage = totalCount/pageSize+totalCount%pageSize;
        if(totalCount>0){
            for(int currentPage=1;currentPage<=totalPage;currentPage++){
                IPage<UserWallet> list = userWalletMapper.selectPage(new Page<>(currentPage,pageSize),queryWrapper);
                list.getRecords().forEach(wallet->{
                    try {
                        WalletBaseRequest request = new WalletBaseRequest();
                        request.setCoin(SupportedCoins.valueOf(wallet.getCoinSymbol()));
                        request.setAccountPrimaryKey(wallet.getWalletKey());
                        WalletOpResult<WalletBaseResult> walletLoadResult = webWalletStrategy.loadWalletKey(request);
                        if(null !=walletLoadResult && walletLoadResult.isOk()
                                && walletLoadResult.getData().isWalletExist() && walletLoadResult.getData().isLoaded()){
                            log.info("load wallet with key success.walletKey:{}",wallet.getWalletKey());
                        }else {
                            log.info("load wallet with key fail.walletKey:{}",wallet.getWalletKey());
                        }
                    }catch (Exception e){
                        log.error("error load wallet.",e);
                    }
                });
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        loadWallet();
    }
}
