package org.eea.dataset.persistence.metabase.repository;

import org.eea.dataset.persistence.metabase.domain.ReleaseReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.Optional;

public interface ReleaseReceiptRepository extends JpaRepository<ReleaseReceipt, Long> {

    Optional<ReleaseReceipt> findByDataflowId(Long dataflowId);
    @Transactional
    void deleteByDataflowId(Long dataflowId);

}
