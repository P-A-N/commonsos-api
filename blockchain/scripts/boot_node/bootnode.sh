BOOT_NODE_PRIVATE_IP=$1
bootnode --nodekey=boot.key --addr $BOOT_NODE_PRIVATE_IP:30301 >>bootnode.log 2>&1 &
