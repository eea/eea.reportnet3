package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
import org.eea.dataset.persistence.metabase.domain.SnapshotSchema;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;


public interface SnapshotSchemaRepository extends CrudRepository<SnapshotSchema, Long> {


  List<SnapshotSchema> findByDesignDatasetIdOrderByCreationDateDesc(
      @Param("idDesignDataset") Long idDataset);


}
