import { SnapshotRepository } from 'repositories/SnapshotRepository';

import { SnapshotUtils } from 'services/_utils/SnapshotUtils';

export const SnapshotService = {
  getAllDesigner: async datasetSchemaId => {
    const snapshots = await SnapshotRepository.getAllDesigner(datasetSchemaId);
    return SnapshotUtils.parseSnapshotListDTO(snapshots.data);
  },

  getAllReporter: async datasetId => {
    const snapshots = await SnapshotRepository.getAllReporter(datasetId);
    return SnapshotUtils.parseSnapshotListDTO(snapshots.data);
  },

  createDesigner: async (datasetId, datasetSchemaId, description) =>
    await SnapshotRepository.createDesigner(datasetId, datasetSchemaId, description),

  createReporter: async (datasetId, description, isReleased) =>
    await SnapshotRepository.createReporter(datasetId, description, isReleased),

  deleteDesigner: async (datasetSchemaId, snapshotId) =>
    await SnapshotRepository.deleteDesigner(datasetSchemaId, snapshotId),

  deleteReporter: async (datasetId, snapshotId) => await SnapshotRepository.deleteReporter(datasetId, snapshotId),

  restoreDesigner: async (datasetSchemaId, snapshotId) =>
    await SnapshotRepository.restoreDesigner(datasetSchemaId, snapshotId),

  restoreReporter: async (dataflowId, datasetId, snapshotId) =>
    await SnapshotRepository.restoreReporter(dataflowId, datasetId, snapshotId),

  release: async (dataflowId, dataProviderId, restrictFromPublic) =>
    await SnapshotRepository.release(dataflowId, dataProviderId, restrictFromPublic)
};
