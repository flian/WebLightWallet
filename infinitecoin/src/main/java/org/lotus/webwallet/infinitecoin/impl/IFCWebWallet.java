package org.lotus.webwallet.infinitecoin.impl;

import com.google.infinitecoinj.core.*;
import com.google.infinitecoinj.params.MainNetParams;
import com.google.infinitecoinj.params.RegTestParams;
import com.google.infinitecoinj.params.TestNet3Params;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.lotus.webwallet.base.api.WalletBlockChainDownloadProcessEventCallback;
import org.lotus.webwallet.base.api.WalletEventListenerCallback;
import org.lotus.webwallet.base.api.dto.*;
import org.lotus.webwallet.base.api.enums.SupportedCoins;
import org.lotus.webwallet.base.config.WebWalletFileConfigProperties;
import org.lotus.webwallet.base.impl.BaseAbstractWebWallet;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

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

    private final WebWalletFileConfigProperties webWalletFileConfigProperties;
    private final WalletBlockChainDownloadProcessEventCallback walletBlockChainDownloadProcessEventCallback;

    public static final String REG_TEST_NET = "regtest";
    private static final String TEST_NET = "test";
    private static final String MAIN_WALLET_NAME_PREFIX = "IFC_MAIN";

    //default fee and fee per kb to 1 IFC
    private  final BigInteger DEFAULT_FEE;
    private  final  BigInteger DEFAULT_FEE_PER_KB;

    private  String regtestHost = "127.0.0.1";

    private  final String ifcNet;
    private final boolean loadCheckPoint;

    protected final NetworkParameters networkParameters;
    protected final IfcMultiWalletAppKit infiniteCoinMainKit;
    protected final CoinNetInfo currentNetInfo;
    public IFCWebWallet(@Value("${web.wallet.ifc.net:regtest}") String net,
                        @Value("${web.wallet.ifc.regtestHost:127.0.0.1}") String regHost,
                        @Value("${web.wallet.ifc.minFee:1}") String minFee,
                        @Value("${web.wallet.ifc.minFeePerKb:1}") String minFeePerKb,
                        @Value("${web.wallet.ifc.loadCheckPointForMainNet:true}") boolean loadCheckPoint,
                        WalletBlockChainDownloadProcessEventCallback walletBlockChainDownloadProcessEventCallback,
                        WebWalletFileConfigProperties webWalletFileConfigProperties){
        this.webWalletFileConfigProperties = webWalletFileConfigProperties;
        this.walletBlockChainDownloadProcessEventCallback = walletBlockChainDownloadProcessEventCallback;
        this.ifcNet = net;
        this.loadCheckPoint=loadCheckPoint;
        DEFAULT_FEE = Utils.toNanoCoins(minFee);
        DEFAULT_FEE_PER_KB =  Utils.toNanoCoins(minFeePerKb);
        currentNetInfo = new CoinNetInfo(SupportedCoins.INFINITE_COIN,"IFC",ifcNet,"");
        if(REG_TEST_NET.equals(ifcNet)){
            //TODO default reg test net
            networkParameters = RegTestParams.get();
            currentNetInfo.setCurrentNetDesc("回归测试网络");
        }else if(TEST_NET.equals(ifcNet)){
         //TODO test net
            networkParameters = TestNet3Params.get();
            currentNetInfo.setCurrentNetDesc("测试网络");
        }else {
            //TODO main net
            networkParameters = MainNetParams.get();
            currentNetInfo.setCurrentNetDesc("主网");

        }
        infiniteCoinMainKit = new IfcMultiWalletAppKit(networkParameters,
                new File(webWalletFileConfigProperties.getCoinRootPath(SupportedCoins.INFINITE_COIN)+File.separator+ifcNet)
                , MAIN_WALLET_NAME_PREFIX);
        if(networkParameters == RegTestParams.get()){
            infiniteCoinMainKit.connectToGivenHost(regHost);
        }
        if(networkParameters == MainNetParams.get()){
            if(loadCheckPoint){
                log.info("enable checkpoint,load checkPoint...");
                infiniteCoinMainKit.setCheckpoints(getClass().getClassLoader().getResourceAsStream("mainNet/checkpoints_20250118"));
                log.info("enable checkpoint,load checkPoint done");
            }
        }
        // Now configure and start the appkit. This will take a second or two - we could show a temporary splash screen
        // or progress widget to keep the user engaged whilst we initialise, but we don't.
        //trade main is done,every thing is down.
        infiniteCoinMainKit.setDownloadListener(new IFCWalletDownloadListener(SupportedCoins.INFINITE_COIN,MAIN_WALLET_NAME_PREFIX,walletBlockChainDownloadProcessEventCallback))
                .setBlockingStartup(false)
                .setUserAgent(MAIN_WALLET_NAME_PREFIX, "1.0")
                .startAndWait();
        // Don't make the user wait for confirmations for now, as the intention is they're sending it their own money!
        infiniteCoinMainKit.wallet().allowSpendingUnconfirmedTransactions();
        infiniteCoinMainKit.peerGroup().setMaxConnections(11);
        log.info("mainWallet info:{}",infiniteCoinMainKit.wallet());
    }

    @Override
    public CoinNetInfo currentNetInfo(SupportedCoins coin) {
        return currentNetInfo;
    }

    @Override
    public WalletOpResult<EnsureWalletResult> ensureWallet(EnsureWalletRequest request,WalletEventListenerCallback eventListenerCallback) {
        if(supportCoin(request.getCoin())){
            if(ObjectUtils.isEmpty(request.getAccountPrimaryKey())){
               return WalletOpResult.fail("accountPrimaryKey is not allow empty.");
            }
            String walletKey = ObjectUtils.isEmpty(request.getPreferWalletKey())?request.getAccountPrimaryKey():String.format("%s_%s",request.getAccountPrimaryKey(),request.getPreferWalletKey());
            Wallet wallet = getWalletByKey(walletKey,request.getPassword(),eventListenerCallback);
            WalletBaseResult baseResult = genCommonResult(walletKey,wallet);
            EnsureWalletResult ensureWalletResult = new EnsureWalletResult();
            BeanUtils.copyProperties(baseResult,ensureWalletResult);
            return WalletOpResult.Ok(ensureWalletResult,SUCCESS);
        }
        return null;
    }

    @Override
    public WalletOpResult<WalletBaseResult> loadWalletKey(WalletBaseRequest request, WalletEventListenerCallback eventListenerCallback) {
        WalletOpResult<WalletBaseResult> result = WalletOpResult.fail(WalletOpResultEnum.FAIL,"");
        if(supportCoin(request.getCoin())) {
            Wallet wallet = infiniteCoinMainKit.ensureLoadWallet(request.getAccountPrimaryKey(), "",false,eventListenerCallback);
            if(null != wallet){
                result.setCode(WalletOpResultEnum.SUCCESS);
                result.setData(genCommonResult(request.getAccountPrimaryKey(),wallet));
                result.getData().setWalletExist(true);
                result.getData().setLoaded(true);
            }else {
                result.setData(new WalletBaseResult());
                result.getData().setWalletExist(false);
                result.getData().setLoaded(false);
            }
        }
        return result;
    }

    private Wallet getWalletByKey(String key,String password,WalletEventListenerCallback eventListenerCallback){
        return infiniteCoinMainKit.ensureLoadWallet(key,password,eventListenerCallback);
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
            Wallet wallet = getWalletByKey(baseRequest.getAccountPrimaryKey(),baseRequest.getPassword(),null);
            return wallet.getChangeAddress().toString();
        }
        return null;
    }

    private boolean isPasswordOk(Wallet wallet,String password){
        if(null == wallet){
            return false;
        }
        if(!wallet.isEncrypted() && ObjectUtils.isEmpty(password)){
            return true;
        }
        return wallet.isEncrypted() && wallet.checkPassword(password);
    }

    @Override
    public WalletOpResult<Boolean> checkWalletPassword(WalletBaseRequest request) {
        WalletOpResult<Boolean> result = WalletOpResult.fail(WalletOpResultEnum.WALLET_PASSWORD_FAIL,"");
        result.setData(false);
        Wallet wallet = infiniteCoinMainKit.getWalletIfPresent(request.getAccountPrimaryKey());
        if(isPasswordOk(wallet,request.getPassword())){
            result = WalletOpResult.Ok(true,SUCCESS);
        }
        return result;
    }

    @Override
    public WalletOpResult<Boolean> changeWalletPassword(ChangeWalletPasswordRequest request) {
        WalletOpResult<Boolean> result = WalletOpResult.fail(WalletOpResultEnum.WALLET_PASSWORD_FAIL,"");
        result.setData(false);
        Wallet wallet = infiniteCoinMainKit.getWalletIfPresent(request.getAccountPrimaryKey());
        if(null != wallet){
            if(isPasswordOk(wallet,request.getPassword())){
                wallet.decrypt(wallet.getKeyCrypter().deriveKey(request.getPassword()));
                wallet.encrypt(request.getNewPassword());
                result = WalletOpResult.Ok(true,SUCCESS);
            }
        }
        return result;
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
            wallet = getWalletByKey(baseRequest.getAccountPrimaryKey(),baseRequest.getPassword(),null);
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
            req.ensureMinRequiredFee = false;
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
            txResult.setTxId(sendResult.tx.getHashAsString());
            txResult.setTxDetail(sendResult.tx.toString());
            return WalletOpResult.Ok(txResult,SUCCESS);
        } catch (AddressFormatException e) {
            log.error("Address error.",e);
            return WalletOpResult.fail(e.getMessage());
        } catch (InsufficientMoneyException e) {
            log.error("exception send coin.",e);
            return WalletOpResult.fail(e.getMessage());
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
