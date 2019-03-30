HOST_IP=`hostname -i`
BOOT_NODE_IP=$1
ETHERBASE=$2
geth --networkid 9239459 --datadir=data --port 30303 --syncmode "full" --rpc --rpcaddr $HOST_IP --rpcport 8545 --rpccorsdomain "*" --rpcapi "admin,eth,net,web3,personal" --ipcdisable --mine --miner.threads=1 --miner.gasprice "0" --miner.gastarget "0" --miner.etherbase=$ETHERBASE --lightserv 50 --bootnodes=enode://4ac77627e4236535c8778b66b0e1c440b190642eb7bd01a18c49f6a0893ebc8009f3d1712d7a5f61322c26be2f888e3baf5c713a394f7b6d7bfd67fa2f6e1dfd@[$BOOT_NODE_IP]:30301 >>miner.log 2>&1 &
