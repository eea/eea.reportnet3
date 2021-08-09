import { SnapshotRepository } from 'repositories/SnapshotRepository';
import { Snapshot } from 'entities/Snapshot';

const parseSnapshotList = response => {
  return response.data.map(
    snapshotDTO =>
      new Snapshot({
        creationDate: snapshotDTO.creationDate,
        description: snapshotDTO.description,
        id: snapshotDTO.id,
        isAutomatic: snapshotDTO.automatic,
        isReleased: snapshotDTO.release
      })
  );
};

export const SnapshotService = {
  getAllDesigner: async datasetSchemaId => parseSnapshotList(await SnapshotRepository.getAllDesigner(datasetSchemaId)),

  getAllReporter: async datasetId => parseSnapshotList(await SnapshotRepository.getAllReporter(datasetId)),

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
