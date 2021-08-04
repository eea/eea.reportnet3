import { ApiSnapshotRepository } from 'repositories/_temp/model/Snapshot/ApiSnapshotRepository';

export const SnapshotRepository = {
  allDesigner: () => Promise.reject('[SnapshotDatasetDesignerRepository#all] must be implemented'),
  allReporter: () => Promise.reject('[SnapshotReporterDatasetRepository#all] must be implemented'),
  createByIdDesigner: () => Promise.reject('[SnapshotDatasetDesignerRepository#createById] must be implemented'),
  createByIdReporter: () => Promise.reject('[SnapshotReporterDatasetRepository#createById] must be implemented'),
  deleteByIdDesigner: () => Promise.reject('[SnapshotDatasetDesignerRepository#deleteById] must be implemented'),
  deleteByIdReporter: () => Promise.reject('[SnapshotReporterDatasetRepository#deleteById] must be implemented'),
  releaseDataflow: () => Promise.reject('[SnapshotReporterDatasetRepository#releaseDataflow] must be implemented'),
  restoreByIdDesigner: () => Promise.reject('[SnapshotDatasetDesignerRepository#restoreById] must be implemented'),
  restoreByIdReporter: () => Promise.reject('[SnapshotReporterDatasetRepository#restoreById] must be implemented')
};

export const snapshotRepository = Object.assign({}, SnapshotRepository, ApiSnapshotRepository);
