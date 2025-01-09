package org.eea.dataset.persistence.metabase.repository;

import org.eea.dataset.persistence.metabase.domain.ReleaseReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReleaseReceiptRepository extends JpaRepository<ReleaseReceipt, Long> {

    // Custom query if needed (optional)
    ReleaseReceipt findByDataflowId(Long dataflowId);
}
