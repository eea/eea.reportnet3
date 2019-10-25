import { apiSnapshot } from 'core/infrastructure/api/domain/model/Snapshot';
import { Snapshot } from 'core/domain/model/Snapshot/Snapshot';

const all = async datasetId => {
  const snapshotsDTO = await apiSnapshot.all(datasetId);
  return snapshotsDTO.map(
    snapshotDTO =>
      new Snapshot(
        snapshotDTO.id,
        snapshotDTO.creationDate,
        snapshotDTO.description,
        snapshotDTO.release,
        undefined,
        undefined,
        undefined
      )
  );
};

const createById = async (datasetId, description) => {
  return await apiSnapshot.createById(datasetId, description);
};

const deleteById = async (datasetId, snapshotId) => {
  const isDeleted = await apiSnapshot.deleteById(datasetId, snapshotId);

  const snapshotToDelete = new Snapshot(snapshotId, undefined, undefined, undefined, undefined, isDeleted, undefined);

  return snapshotToDelete;
};

const restoreById = async (dataflowId, datasetId, snapshotId) => {
  return await apiSnapshot.restoreById(dataflowId, datasetId, snapshotId);
};

const releaseById = async (dataflowId, datasetId, snapshotId) => {
  return await apiSnapshot.releaseById(dataflowId, datasetId, snapshotId);
};

export const ApiSnapshotRepository = {
  all,
  createById,
  deleteById,
  restoreById,
  releaseById
};
