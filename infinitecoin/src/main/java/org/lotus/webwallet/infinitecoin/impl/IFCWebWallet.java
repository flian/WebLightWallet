package org.lotus.webwallet.infinitecoin.impl;

import com.google.infinitecoinj.core.*;

import com.google.infinitecoinj.params.MainNetParams;
import com.google.infinitecoinj.params.RegTestParams;
import com.google.infinitecoinj.params.TestNet3Params;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.lotus.webwallet.base.api.dto.*;
import org.lotus.webwallet.base.api.enums.SupportedCoins;
import org.lotus.webwallet.base.config.WebWalletFileConfigProperties;
import org.lotus.webwallet.base.impl.BaseAbstractWebWallet;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;


import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;


import static com.google.infinitecoinj.core.CoinDefinition.DUST_LIMIT;

/**
 * @author : foy
 * @date : 2024/12/22:16:59
 **/
@Slf4j
@Getter
@Setter
@Service(value = Constants.INFINITE_COIN)
public class IFCWebWallet extends BaseAbstractWebWallet {
    @Resource
    private WebWalletFileConfigProperties webWalletFileConfigProperties;

    public static final String REG_TEST_NET = "regtest";
    private static final String TEST_NET = "test";
    private static final String MAIN_WALLET_NAME_PREFIX = "IFC_MAIN";

    //default fee and fee per kb to 1 IFC
    private  final BigInteger DEFAULT_FEE;
    private  final  BigInteger DEFAULT_FEE_PER_KB;

    private  String regtestHost = "127.0.0.1";

    private  final String ifcNet;

    protected final NetworkParameters networkParameters;
    protected final IfcMultiWalletAppKit infiniteCoinMainKit;
    public IFCWebWallet(@Value("${web.wallet.ifc.net:regtest}") String net,
                        @Value("${web.wallet.ifc.regtestHost:127.0.0.1}") String regHost,
                        @Value("${web.wallet.ifc.minFee:1}") String minFee,
                        @Value("${web.wallet.ifc.minFeePerKb:1}") String minFeePerKb){
        this.ifcNet = net;
        DEFAULT_FEE = Utils.toNanoCoins(minFee);
        DEFAULT_FEE_PER_KB =  Utils.toNanoCoins(minFeePerKb);
        if(REG_TEST_NET.equals(ifcNet)){
            //TODO default reg test net
            networkParameters = RegTestParams.get();
        }else if(TEST_NET.equals(ifcNet)){
         //TODO test net
            networkParameters = TestNet3Params.get();
        }else {
            //TODO main net
            networkParameters = MainNetParams.get();
        }
        infiniteCoinMainKit = new IfcMultiWalletAppKit(networkParameters,
                new File(webWalletFileConfigProperties.getCoinRootPath(SupportedCoins.INFINITE_COIN)+File.separator+ifcNet)
                , MAIN_WALLET_NAME_PREFIX);
        if(networkParameters == RegTestParams.get()){
            infiniteCoinMainKit.connectToGivenHost(regHost);
        }
        // Now configure and start the appkit. This will take a second or two - we could show a temporary splash screen
        // or progress widget to keep the user engaged whilst we initialise, but we don't.
        infiniteCoinMainKit.setDownloadListener(new DownloadListener())
                .setBlockingStartup(false)
                .setUserAgent(MAIN_WALLET_NAME_PREFIX, "1.0")
                .startAndWait();
        // Don't make the user wait for confirmations for now, as the intention is they're sending it their own money!
        infiniteCoinMainKit.wallet().allowSpendingUnconfirmedTransactions();
        infiniteCoinMainKit.peerGroup().setMaxConnections(11);
        log.info("mainWallet info:{}",infiniteCoinMainKit.wallet());
    }
    @Override
    public WalletOpResult<EnsureWalletResult> ensureWallet(EnsureWalletRequest request) {
        if(supportCoin(request.getCoin())){
            if(ObjectUtils.isEmpty(request.getAccountPrimaryKey())){
               return WalletOpResult.fail("accountPrimaryKey is not allow empty.");
            }
            String walletKey = ObjectUtils.isEmpty(request.getPreferWalletKey())?request.getAccountPrimaryKey():String.format("%s_%s",request.getAccountPrimaryKey(),request.getPreferWalletKey());
            Wallet wallet = getWalletByKey(walletKey,request.getPassword());
            WalletBaseResult baseResult = genCommonResult(walletKey,wallet);
            EnsureWalletResult ensureWalletResult = new EnsureWalletResult();
            BeanUtils.copyProperties(baseResult,ensureWalletResult);
            return WalletOpResult.Ok(ensureWalletResult,"SUCCESS");
        }
        return null;
    }

    private Wallet getWalletByKey(String key,String password){
        return infiniteCoinMainKit.ensureLoadWallet(key,password);
    }

    private WalletBaseResult genCommonResult(String walletKey,Wallet wallet){
        WalletBaseResult result = new WalletBaseResult();
        result.setBase58Address(wallet.getChangeAddress().toString());
        result.setBalance(getHumReadableBalance(wallet));
        result.setBalanceBetterShowValue(Utils.bitcoinValueToFriendlyString(getBalance(wallet)));
        result.setWalletKey(walletKey);
        return result;
    }

    private BigInteger getBalance(Wallet wallet){
        return wallet.getBalance(Wallet.BalanceType.ESTIMATED);
    }

    private BigDecimal getHumReadableBalance(Wallet wallet){
        return new BigDecimal(getBalance(wallet), 8);
    }

    @Override
    public String getAddress( WalletBaseRequest baseRequest) {
        if(supportCoin(baseRequest.getCoin())){
            Wallet wallet = getWalletByKey(baseRequest.getAccountPrimaryKey(),baseRequest.getPassword());
            return wallet.getChangeAddress().toString();
        }
        return null;
    }

    @Override
    public WalletOpResult<TransferResult> transferToAddress(WalletBaseRequest baseRequest, String base58ToAddress, BigDecimal amount,String base58ChangeAddress) {
        Wallet wallet = null;
        if(supportCoin(baseRequest.getCoin()) && valid2Address(base58ToAddress,baseRequest.getCoin())){
            if(DUST_LIMIT.compareTo(Utils.toNanoCoins(amount.toString()))>0){
                //dust transaction is not allow for now..
                log.error("min send 1000ifc for now, it will be trande as dust lest than 1000 and confirm..");
                return WalletOpResult.fail("min 1000 ifc to transfer, other wise will be trade as dust..");
            }
            wallet = getWalletByKey(baseRequest.getAccountPrimaryKey(),baseRequest.getPassword());
            BigDecimal balance = getHumReadableBalance(wallet);
            if(balance.compareTo(amount) < 0){
                log.error("not sufficient funds,balance:{},transfer amount:{}",balance,amount);
                return WalletOpResult.fail(WalletOpResultEnum.NOT_SUFFICIENT_FUNDS,String.format("not sufficient funds,balance:%s,transfer amount:%s",balance,amount));
            }
            if(wallet.isEncrypted() & !wallet.checkPassword(baseRequest.getPassword())){
                log.error("wallet password fail");
                return WalletOpResult.fail(WalletOpResultEnum.WALLET_PASSWORD_FAIL,String.format("not sufficient funds,balance:%s,transfer amount:%s",balance,amount));
            }
        }else {
            log.info("not support or not valid coin, return.");
            return WalletOpResult.fail(WalletOpResultEnum.NOT_SUPPORT_COIN,String.format("not support coin:%s or invalid address:%s.",baseRequest.getCoin(),base58ToAddress));
        }

        try {
            Address destination  = new Address(infiniteCoinMainKit.params, base58ToAddress);
            Wallet.SendRequest req = Wallet.SendRequest.to(destination,Utils.toNanoCoins(amount.toString()));
            //set fee and fee perKb to 1
            req.fee = DEFAULT_FEE;
            req.feePerKb = DEFAULT_FEE_PER_KB;
            req.emptyWallet = false;
            Address changeAddress = wallet.getChangeAddress();
            if(valid2Address(base58ChangeAddress,baseRequest.getCoin())){
                changeAddress = new Address(infiniteCoinMainKit.params,base58ChangeAddress);
            }
            log.info("set change address:{}",changeAddress.toString());
            req.changeAddress = changeAddress;
            if(wallet.isEncrypted()){
                req.aesKey = wallet.getKeyCrypter().deriveKey(baseRequest.getPassword());
            }
            Wallet.SendResult sendResult = wallet.sendCoins(req);
            TransferResult txResult = new TransferResult();
            txResult.setTxId(sendResult.tx.toString());
            return WalletOpResult.Ok(txResult,"SUCCESS");
        } catch (AddressFormatException e) {
            log.error("Address error.",e);
            throw new RuntimeException(e);
        } catch (InsufficientMoneyException e) {
            log.error("exception send coin.",e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean valid2Address(String base58ToAddress, SupportedCoins coin) {
        if(supportCoin(coin)){
            try {
                Address address = new Address(infiniteCoinMainKit.params,base58ToAddress);
                if(!address.toString().equals(base58ToAddress)){
                    log.info("address invalid.");
                    return false;
                }
                log.info("address is valid.");
                return true;
            } catch (AddressFormatException e) {
                log.info("address is invalid.");
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean supportCoin(SupportedCoins coin) {
        return coin.equals(SupportedCoins.INFINITE_COIN);
    }

    @Override
    public String netInfo(SupportedCoins coin) {
        return supportCoin(coin)?ifcNet:null;
    }
}
