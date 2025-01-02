package co.yiiu.pybbs.service.impl;

import co.yiiu.pybbs.mapper.RsaPrivatePubKeyMapper;
import co.yiiu.pybbs.model.RsaPrivatePubKey;
import co.yiiu.pybbs.service.UserWalletService;
import co.yiiu.pybbs.service.vo.RsaPubKeyInfoForFrontDto;
import co.yiiu.pybbs.service.vo.WalletKeyAndPasswordInfoInitRequestDto;
import co.yiiu.pybbs.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * @author : foy
 * @date : 2025/1/2:21:36
 **/


@Slf4j
@Service
public class UserWalletServiceImpl implements UserWalletService {
    protected static final String WALLET_ENCRYPTED_METHOD_KEY = "RSA";
    protected static final int DEFAULT_RSA_KEY_SIZE = 2048;

    @Resource
    private RsaPrivatePubKeyMapper rsaPrivatePubKeyMapper;

    @Override
    public boolean genAndSavePrivateKeys(int count) {
        try {
            if(count > 0){
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
        return null;
    }

    @Override
    public boolean initOrChangePasswordForWallet(String userName, WalletKeyAndPasswordInfoInitRequestDto requestDto) {
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
        KeyPairGenerator generator = KeyPairGenerator.getInstance(WALLET_ENCRYPTED_METHOD_KEY);
        generator.initialize(keySize);
        return generator.generateKeyPair();
    }

}
