# WebLightWallet

a simple web light wallet for infinitecoin and dogm coin

a bbs sample include with infinitecoin and dogmcoin

# infinitecoinj



### test in regtest net

build and install:

https://github.com/flian/infinitecoinj


create infinitecoin.conf in /home/{user}/.infinitecoin with config:


`

regtest=1
rpcuser=test
rpcpassword=abcd11111

`

start qt with regtest net:

```
./infinitecoin-qt -regtest -rescan -reindex -connect=0 -txindex -server -rest -bind=127.0.0.1 -printtoconsole -datadir=/home/infinitecoindata
```


get new address:

```
./infinitecoin-cli  -regtest  getnewaddress
```


gen new blockchain:

```
./infinitecoin-cli -regtest -rpcuser=test -rpcpassword=abcd11111 generate 150
```
