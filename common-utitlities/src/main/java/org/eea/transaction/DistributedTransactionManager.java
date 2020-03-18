package org.eea.transaction;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Distributed transaction manager.
 */
public class DistributedTransactionManager {

  private static ThreadLocal<UUID> localThread = new ThreadLocal<>();
  private static Map<UUID, DistributedTransacion> transacionMap = new ConcurrentHashMap<>();

  /**
   * Create transacion.
   *
   * @return the distributed transacion
   */
  public static DistributedTransacion createTransacion() {
    UUID transactionId = UUID.randomUUID();
    localThread.set(transactionId);
    DistributedTransacion transaction = new DistributedTransacion(transactionId);
    transacionMap.put(transactionId, transaction);
    return transaction;
  }

  /**
   * Finish transaction passed as parameter.
   *
   * @param transactionId the transaction id
   */
  public static void finishTransaction(UUID transactionId) {
    localThread.remove();
    transacionMap.remove(transactionId);
  }
}
