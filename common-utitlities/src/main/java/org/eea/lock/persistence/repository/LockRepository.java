package org.eea.lock.persistence.repository;

import org.eea.lock.persistence.domain.Lock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Interface LockRepository.
 */
public interface LockRepository extends CrudRepository<Lock, Integer> {

  /**
   * Save if absent.
   *
   * @param id the id
   * @param lock the lock
   * @return true, if successful
   */
  @Transactional
  default boolean saveIfAbsent(Integer id, Lock lock) {

    if (findById(id).isPresent()) {
      return false;
    }

    save(lock);
    return true;
  }

  /**
   * Delete if present.
   *
   * @param id the id
   * @return true, if successful
   */
  @Transactional
  default boolean deleteIfPresent(Integer id) {
    if (!findById(id).isPresent()) {
      return false;
    }

    deleteById(id);
    return true;
  }
}
