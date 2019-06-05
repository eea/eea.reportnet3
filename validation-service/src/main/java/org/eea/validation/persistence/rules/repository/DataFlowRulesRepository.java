package org.eea.validation.persistence.rules.repository;

import java.util.List;
import org.bson.types.ObjectId;
import org.eea.validation.persistence.rules.model.DataFlowRule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * The Interface DataSetSchemaRepository.
 */
@Repository
public interface DataFlowRulesRepository extends MongoRepository<DataFlowRule, ObjectId> {


  List<DataFlowRule> findAllByDataFlowId(Long idDataflow);
}
