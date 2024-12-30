package org.lotus.webwallet.infinitecoin.impl;

import com.google.infinitecoinj.core.BlockChain;
import com.google.infinitecoinj.core.NetworkParameters;
import com.google.infinitecoinj.core.Wallet;
import com.google.infinitecoinj.store.SPVBlockStore;
import lombok.Data;

import java.io.File;

/**
 * @author : foy
 * @date : 2024/12/30:16:24
 **/
@Data
public class IfcWalletAndChainData {
    private String walletKey;
    private Wallet wallet;
    private File vWalletFile;
    private BlockChain vChain;
    private SPVBlockStore vStore;
    private NetworkParameters networkParameters;
}
