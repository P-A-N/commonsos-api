package commonsos.service.blockchain;

import io.reactivex.functions.Consumer;

public abstract class BlockchainConsumer<T> implements Consumer<T> {

  public abstract boolean isDone();
}
