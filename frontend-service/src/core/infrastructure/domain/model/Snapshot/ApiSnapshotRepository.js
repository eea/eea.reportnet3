import { apiSnapshot } from 'core/infrastructure/api/domain/model/Snapshot';
import { Snapshot } from 'core/domain/model/Snapshot/Snapshot';

const all = async datasetId => {
  const snapshotsDTO = await apiSnapshot.all(datasetId);
  return snapshotsDTO.map(
    snapshotDTO => new Snapshot(snapshotDTO.id, snapshotDTO.creationDate, snapshotDTO.description, snapshotDTO.release)
  );
};

const createById = async (datasetId, description) => {
  const isCreated = await apiSnapshot.createById(datasetId, description);

  const snapshotToCreate = new Snapshot();
  snapshotToCreate.description = description;
  snapshotToCreate.isCreated = isCreated;

  return snapshotToCreate;
};

const deleteById = async (datasetId, snapshotId) => {
  const isDeleted = await apiSnapshot.deleteById(datasetId, snapshotId);

  const snapshotToDelete = new Snapshot();
  snapshotToDelete.id = snapshotId;
  snapshotToDelete.isDeleted = isDeleted;

  return snapshotToDelete;
};

const restoreById = async (dataflowId, datasetId, snapshotId) => {
  const isRestored = await apiSnapshot.restoreById(dataflowId, datasetId, snapshotId);

  const snapshotToRestore = new Snapshot();
  snapshotToRestore.id = snapshotId;
  snapshotToRestore.isRestored = isRestored;

  return snapshotToRestore;
};

const releaseById = async (dataflowId, datasetId, snapshotId) => {
  const isReleased = await apiSnapshot.releaseById(dataflowId, datasetId, snapshotId);

  const snapshotToRelease = new Snapshot();
  snapshotToRelease.id = snapshotId;
  snapshotToRelease.isReleased = isReleased;

  return snapshotToRelease;
};

export const ApiSnapshotRepository = {
  all,
  createById,
  deleteById,
  restoreById,
  releaseById
};
