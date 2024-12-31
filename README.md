# WebLightWallet

a simple web light wallet for infinitecoin and dogm coin

a bbs sample include with infinitecoin and dogmcoin

## install vmware-tools in linux

```
see example: https://blog.csdn.net/harebert/article/details/143193921

sudo apt-get update
sudo apt-get install open-vm-tools open-vm-tools-desktop
vmware-toolbox-cmd -v
retart system

```

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

### sample command

./infinitecoin-qt -regtest -connect=0 -txindex -bind=192.168.2.7 -server -rest -printtoconsole -datadir=/home/infinitecoindata

./infinitecoin-cli -regtest -rpcuser=test -rpcpassword=abcd11111 generate 6

./infinitecoin-cli -regtest -rpcuser=test -rpcpassword=abcd11111 generatetoaddress 10 mgcDiBPwhboxyaWaqCWYHiugd1AjaMM2Pj

./infinitecoin-cli -regtest -rpcuser=test -rpcpassword=abcd11111 generatetoaddress 6 "mgcDiBPwhboxyaWaqCWYHiugd1AjaMM2Pj"



### bbs

bbs change from pybbs see:[original pybbs](https://github.com/atjiu/pybbs)
