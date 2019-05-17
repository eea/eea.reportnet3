/**
 * 
 */
package org.eea.dataset.schemas.repository;

import org.eea.dataset.schemas.domain.DatabaseSequences;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Mario Severa
 *
 */
public interface SequenceRepository extends MongoRepository<DatabaseSequences, String> {

}
