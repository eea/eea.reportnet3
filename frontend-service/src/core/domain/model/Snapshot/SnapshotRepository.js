import { ApiSnapshotRepository } from 'core/infrastructure/domain/model/Snapshot/ApiSnapshotRepository';

export const SnapshotRepository = {
  all: () => Promise.reject('[SnapshotRepository#all] must be implemented')
};

export const snapshotRepository = Object.assign({}, SnapshotRepository, ApiSnapshotRepository);
