package org.eea.dataset.persistence.metabase.repository;

import org.eea.dataset.persistence.metabase.domain.ReleaseReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReleaseReceiptRepository extends JpaRepository<ReleaseReceipt, Long> {

    // Custom query if needed (optional)
    Optional<ReleaseReceipt> findByDataflowId(Long dataflowId);
}
