package org.eea.dataset.service.impl;

import lombok.AllArgsConstructor;
import org.eea.dataset.persistence.metabase.repository.SnapshotRepository;
import org.eea.dataset.service.ResolveSnapshotTable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ResolveSnapshotTableImpl implements ResolveSnapshotTable {

  private final SnapshotRepository snapshotRepository;

  @Override
  public void rollBackSnapshotTableValues(Long jobId) {
    
  }
}
