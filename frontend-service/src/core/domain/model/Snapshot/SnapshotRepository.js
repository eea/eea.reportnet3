import { ApiSnapshotRepository } from 'core/infrastructure/domain/model/Snapshot/ApiSnapshotRepository';

export const SnapshotRepository = {
  all: () => Promise.reject('[SnapshotRepository#all] must be implemented'),
  createById: () => Promise.reject('[SnapshotRepository#createById] must be implemented'),
  deleteById: () => Promise.reject('[SnapshotRepository#deleteById] must be implemented'),
  retoreById: () => Promise.reject('[SnapshotRepository#retoreById] must be implemented')
};

export const snapshotRepository = Object.assign({}, SnapshotRepository, ApiSnapshotRepository);
