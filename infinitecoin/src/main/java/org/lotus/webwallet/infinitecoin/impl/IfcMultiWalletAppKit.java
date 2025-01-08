/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lotus.webwallet.infinitecoin.impl;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.infinitecoinj.core.*;
import com.google.infinitecoinj.net.discovery.DnsDiscovery;
import com.google.infinitecoinj.store.BlockStoreException;
import com.google.infinitecoinj.store.SPVBlockStore;
import com.google.infinitecoinj.store.WalletProtobufSerializer;
import lombok.extern.slf4j.Slf4j;
import org.lotus.webwallet.base.api.WalletEventListenerCallback;
import org.lotus.webwallet.base.api.enums.SupportedCoins;
import org.lotus.webwallet.base.exceptions.BizException;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * <p>Utility class that wraps the boilerplate needed to set up a new SPV bitcoinj app. Instantiate it with a directory
 * and file prefix, optionally configure a few things, then use start or startAndWait. The object will construct and
 * configure a {@link BlockChain}, {@link SPVBlockStore}, {@link Wallet} and {@link PeerGroup}. Depending on the value
 * of the blockingStartup property, startup will be considered complete once the block chain has fully synchronized,
 * so it can take a while.</p>
 *
 * <p>To add listeners and modify the objects that are constructed, you can either do that by overriding the
 * {@link #onSetupCompleted()} method (which will run on a background thread) and make your changes there,
 * or by waiting for the service to start and then accessing the objects from wherever you want. However, you cannot
 * access the objects this class creates until startup is complete.</p>
 *
 * <p>The asynchronous design of this class may seem puzzling (just use {@link #startAndWait()} if you don't want that).
 * It is to make it easier to fit bitcoinj into GUI apps, which require a high degree of responsiveness on their main
 * thread which handles all the animation and user interaction. Even when blockingStart is false, initializing bitcoinj
 * means doing potentially blocking file IO, generating keys and other potentially intensive operations. By running it
 * on a background thread, there's no risk of accidentally causing UI lag.</p>
 *
 * <p>Note that {@link #startAndWait()} can throw an unchecked {@link com.google.common.util.concurrent.UncheckedExecutionException}
 * if anything goes wrong during startup - you should probably handle it and use {@link Exception#getCause()} to figure
 * out what went wrong more precisely. Same thing if you use the async start() method.</p>
 */
@Slf4j
public class IfcMultiWalletAppKit extends AbstractIdleService {
    protected final String filePrefix;
    protected final NetworkParameters params;
    protected volatile BlockChain vMainChain;
    protected volatile SPVBlockStore vMainStore;
    protected volatile Wallet vMainWallet;
    protected volatile PeerGroup vPeerGroup;

    protected final File directory;
    protected volatile File vWalletFile;

    protected boolean useAutoSave = true;
    protected PeerAddress[] peerAddresses;
    protected PeerEventListener downloadListener;
    protected boolean autoStop = true;
    protected InputStream checkpoints;
    protected boolean blockingStartup = true;
    protected String userAgent, version;

    protected int walletKeysSize = 10000;
    protected Map<String,IfcWalletAndChainData> walletKeyHashMap = new ConcurrentHashMap<>(walletKeysSize);

    protected Map<String,IFCWalletEventListener> walletEventListenerMap = new ConcurrentHashMap<>(walletKeysSize);


    protected boolean useSingleBlockChainIfo = true;

    protected String defaultMainPassword = "thisIsTestPasswordPlsChangeit123!";
    public boolean isWalletKeyValid(String walletKey){
        if(ObjectUtils.isEmpty(walletKey) || filePrefix.equals(walletKey)){
            log.info("invalid wallet key:{}",walletKey);
            return false;
        }
        return !walletKeyHashMap.containsKey(walletKey);
    }

    public Wallet getWalletByKey(String walletKey){
        if(!walletKeyHashMap.containsKey(walletKey)){
            throw new BizException("can't find wallet.for key:"+walletKey);
        }
        return walletKeyHashMap.get(walletKey).getWallet();

    }

    public Wallet getWalletIfPresent(String walletKey){
        if(ObjectUtils.isEmpty(walletKey)){
            throw new BizException("wallet key is null,try a new name?.for key:"+walletKey);
        }
        Wallet result = null;
        if(walletKeyHashMap.containsKey(walletKey)){
            result = walletKeyHashMap.get(walletKey).getWallet();
        }
        return result;
    }

    public Wallet ensureLoadWallet(String walletKey,String defaultPasswordIfNotThere,WalletEventListenerCallback callback){
        return ensureLoadWallet(walletKey,defaultPasswordIfNotThere,true,callback);
    }

    public Wallet ensureLoadWallet(String walletKey, String defaultPasswordIfNotThere, boolean createIfNotFound, WalletEventListenerCallback callback){
        if(ObjectUtils.isEmpty(walletKey)){
            throw new BizException("wallet key is null,try a new name?.for key:"+walletKey);
        }
        Wallet result = null;
        if(walletKeyHashMap.containsKey(walletKey)){
            result = walletKeyHashMap.get(walletKey).getWallet();
        }
        if(null != result){
            return result;
        }
        if(useSingleBlockChainIfo){
            try{
                IfcWalletAndChainData newWalletData = new IfcWalletAndChainData();
                newWalletData.setWalletKey(walletKey);
                newWalletData.setVWalletFile(new File(directory, walletKey + ".wallet"));
                newWalletData.setNetworkParameters(params);
                File newWalletFile = newWalletData.getVWalletFile();
                Wallet newWallet = null;
                if (newWalletFile.exists()) {
                    FileInputStream walletStream = new FileInputStream(newWalletFile);
                    newWallet = new Wallet(params);
                    new WalletProtobufSerializer().readWallet(WalletProtobufSerializer.parseToProto(walletStream), newWallet);
                } else {
                    if(!createIfNotFound){
                        log.info("don't create wallet,but not wallet found,return null,walletKey:{},createIfNotFound:{}",walletKey,createIfNotFound);
                        return null;
                    }
                    newWallet = new Wallet(params);
                    newWallet.addKey(new ECKey());
                    if(!ObjectUtils.isEmpty(defaultPasswordIfNotThere)){
                        newWallet.encrypt(defaultPasswordIfNotThere);
                    }
                }
                newWalletData.setWallet(newWallet);
                if (useAutoSave) newWallet.autosaveToFile(newWalletFile, 1, TimeUnit.SECONDS, null);
                //fist create and save now ensure no data lost.
                newWallet.saveToFile(newWalletFile);
                if(null != callback && !walletEventListenerMap.containsKey(walletKey)){
                    newWallet.addEventListener(new IFCWalletEventListener(SupportedCoins.INFINITE_COIN,walletKey,callback));
                }

                result = newWalletData.getWallet();
                vMainChain.addWallet(newWallet);
                vPeerGroup.addWallet(newWallet);
                walletKeyHashMap.put(walletKey,newWalletData);
            }catch (Exception e){
                log.error("error load wallet..",e);
            }
        }
        //TODO not use single blockchain data?
        return result;
    }

    public IfcMultiWalletAppKit(NetworkParameters params, File directory, String filePrefix) {
        this.params = checkNotNull(params);
        this.directory = checkNotNull(directory);
        this.filePrefix = checkNotNull(filePrefix);
    }

    /** Will only connect to the given addresses. Cannot be called after startup. */
    public IfcMultiWalletAppKit setPeerNodes(PeerAddress... addresses) {
        checkState(state() == State.NEW, "Cannot call after startup");
        this.peerAddresses = addresses;
        return this;
    }

    public IfcMultiWalletAppKit connectToGivenHost(String ip){
        if(null == ip && ip.isEmpty()){
            return connectToLocalHost();
        }
        try {
            final InetAddress localHost =  InetAddress.getByName(ip);
            return setPeerNodes(new PeerAddress(localHost, params.getPort()));
        } catch (UnknownHostException e) {
            // Borked machine with no loopback adapter configured properly.
            throw new RuntimeException(e);
        }
    }

    /** Will only connect to localhost. Cannot be called after startup. */
    public IfcMultiWalletAppKit connectToLocalHost() {
        try {
            final InetAddress localHost = InetAddress.getLocalHost();
            return setPeerNodes(new PeerAddress(localHost, params.getPort()));
        } catch (UnknownHostException e) {
            // Borked machine with no loopback adapter configured properly.
            throw new RuntimeException(e);
        }
    }

    /** If true, the wallet will save itself to disk automatically whenever it changes. */
    public IfcMultiWalletAppKit setAutoSave(boolean value) {
        checkState(state() == State.NEW, "Cannot call after startup");
        useAutoSave = value;
        return this;
    }

    /**
     * If you want to learn about the sync process, you can provide a listener here. For instance, a
     * {@link DownloadListener} is a good choice.
     */
    public IfcMultiWalletAppKit setDownloadListener(PeerEventListener listener) {
        this.downloadListener = listener;
        return this;
    }

    /** If true, will register a shutdown hook to stop the library. Defaults to true. */
    public IfcMultiWalletAppKit setAutoStop(boolean autoStop) {
        this.autoStop = autoStop;
        return this;
    }

    /**
     * If set, the file is expected to contain a checkpoints file calculated with BuildCheckpoints. It makes initial
     * block sync faster for new users - please refer to the documentation on the bitcoinj website for further details.
     */
    public IfcMultiWalletAppKit setCheckpoints(InputStream checkpoints) {
        this.checkpoints = checkNotNull(checkpoints);
        return this;
    }

    /**
     * If true (the default) then the startup of this service won't be considered complete until the network has been
     * brought up, peer connections established and the block chain synchronised. Therefore {@link #startAndWait()} can
     * potentially take a very long time. If false, then startup is considered complete once the network activity
     * begins and peer connections/block chain sync will continue in the background.
     */
    public IfcMultiWalletAppKit setBlockingStartup(boolean blockingStartup) {
        this.blockingStartup = blockingStartup;
        return this;
    }

    /**
     * Sets the string that will appear in the subver field of the version message.
     * @param userAgent A short string that should be the name of your app, e.g. "My Wallet"
     * @param version A short string that contains the version number, e.g. "1.0-BETA"
     */
    public IfcMultiWalletAppKit setUserAgent(String userAgent, String version) {
        this.userAgent = checkNotNull(userAgent);
        this.version = checkNotNull(version);
        return this;
    }

    /**
     * <p>Override this to load all wallet extensions if any are necessary.</p>
     *
     * <p>When this is called, chain(), store(), and peerGroup() will return the created objects, however they are not
     * initialized/started</p>
     */
    protected void addWalletExtensions() throws Exception { }

    /**
     * This method is invoked on a background thread after all objects are initialised, but before the peer group
     * or block chain download is started. You can tweak the objects configuration here.
     */
    protected void onSetupCompleted() { }

    @Override
    protected void startUp() throws Exception {
        // Runs in a separate thread.
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("Could not create named directory.");
            }
        }
        FileInputStream walletStream = null;
        try {
            File chainFile = new File(directory, filePrefix + ".spvchain");
            boolean chainFileExists = chainFile.exists();
            vWalletFile = new File(directory, filePrefix + ".wallet");
            boolean shouldReplayWallet = vWalletFile.exists() && !chainFileExists;

            vMainStore = new SPVBlockStore(params, chainFile);
            if (!chainFileExists && checkpoints != null) {
                // Ugly hack! We have to create the wallet once here to learn the earliest key time, and then throw it
                // away. The reason is that wallet extensions might need access to peergroups/chains/etc so we have to
                // create the wallet later, but we need to know the time early here before we create the BlockChain
                // object.
                long time = Long.MAX_VALUE;
                if (vWalletFile.exists()) {
                    Wallet wallet = new Wallet(params);
                    FileInputStream stream = new FileInputStream(vWalletFile);
                    new WalletProtobufSerializer().readWallet(WalletProtobufSerializer.parseToProto(stream), wallet);
                    time = wallet.getEarliestKeyCreationTime();
                }
                CheckpointManager.checkpoint(params, checkpoints, vMainStore, time);
            }
            vMainChain = new BlockChain(params, vMainStore);
            vPeerGroup = createPeerGroup();
            vPeerGroup.setBloomFilterFalsePositiveRate(0.0001);
            if (this.userAgent != null)
                vPeerGroup.setUserAgent(userAgent, version);
            if (vWalletFile.exists()) {
                walletStream = new FileInputStream(vWalletFile);
                vMainWallet = new Wallet(params);
                addWalletExtensions(); // All extensions must be present before we deserialize
                new WalletProtobufSerializer().readWallet(WalletProtobufSerializer.parseToProto(walletStream), vMainWallet);
                if (shouldReplayWallet)
                    vMainWallet.clearTransactions(0);
            } else {
                vMainWallet = new Wallet(params);
                vMainWallet.addKey(new ECKey());
                vMainWallet.encrypt(defaultMainPassword);
                addWalletExtensions();
            }
            if (useAutoSave) vMainWallet.autosaveToFile(vWalletFile, 1, TimeUnit.SECONDS, null);
            // Set up peer addresses or discovery first, so if wallet extensions try to broadcast a transaction
            // before we're actually connected the broadcast waits for an appropriate number of connections.
            if (peerAddresses != null) {
                for (PeerAddress addr : peerAddresses) vPeerGroup.addAddress(addr);
                peerAddresses = null;
            } else {
                vPeerGroup.addPeerDiscovery(new DnsDiscovery(params));
            }
            vMainChain.addWallet(vMainWallet);
            vPeerGroup.addWallet(vMainWallet);
            onSetupCompleted();

            if (blockingStartup) {
                vPeerGroup.startAndWait();
                // Make sure we shut down cleanly.
                installShutdownHook();
                // TODO: Be able to use the provided download listener when doing a blocking startup.
                final DownloadListener listener = new DownloadListener();
                vPeerGroup.startBlockChainDownload(listener);
                listener.await();
            } else {
                Futures.addCallback(vPeerGroup.start(), new FutureCallback<State>() {
                    @Override
                    public void onSuccess(State result) {
                        final PeerEventListener l = downloadListener == null ? new DownloadListener() : downloadListener;
                        vPeerGroup.startBlockChainDownload(l);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        throw new RuntimeException(t);
                    }
                });
            }
        } catch (BlockStoreException e) {
            throw new IOException(e);
        } finally {
            if (walletStream != null) walletStream.close();
        }
    }

    protected PeerGroup createPeerGroup() {
        return new PeerGroup(params, vMainChain);
    }

    private void installShutdownHook() {
        if (autoStop) Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override public void run() {
                try {
                    IfcMultiWalletAppKit.this.stopAndWait();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    protected void shutDown() throws Exception {
        // Runs in a separate thread.
        try {
            vPeerGroup.stopAndWait();
            vMainWallet.saveToFile(vWalletFile);
            walletKeyHashMap.keySet().forEach(key ->{
                try {
                    IfcWalletAndChainData customerWalletData = walletKeyHashMap.get(key);
                    customerWalletData.getWallet().saveToFile(customerWalletData.getVWalletFile());
                    customerWalletData.setWallet(null);
                } catch (IOException e) {
                    log.error("save wallet fail.",e);
                }
            });
            vMainStore.close();
            vPeerGroup = null;
            vMainWallet = null;
            vMainStore = null;
            vMainChain = null;
        } catch (BlockStoreException e) {
            throw new IOException(e);
        }
    }

    public NetworkParameters params() {
        return params;
    }

    public BlockChain chain() {
        checkState(state() == State.STARTING || state() == State.RUNNING, "Cannot call until startup is complete");
        return vMainChain;
    }

    public SPVBlockStore store() {
        checkState(state() == State.STARTING || state() == State.RUNNING, "Cannot call until startup is complete");
        return vMainStore;
    }

    public Wallet wallet() {
        checkState(state() == State.STARTING || state() == State.RUNNING, "Cannot call until startup is complete");
        return vMainWallet;
    }

    public PeerGroup peerGroup() {
        checkState(state() == State.STARTING || state() == State.RUNNING, "Cannot call until startup is complete");
        return vPeerGroup;
    }

    public File directory() {
        return directory;
    }
}
