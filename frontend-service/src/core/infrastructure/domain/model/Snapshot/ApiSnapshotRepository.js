import { apiSnapshot } from 'core/infrastructure/api/domain/model/Snapshot';
import { Snapshot } from 'core/domain/model/Snapshot/Snapshot';

const all = async dataSetId => {
  const snapshotsDTO = await apiSnapshot.all(dataSetId);
  return snapshotsDTO.map(
    snapshotDTO => new Snapshot(snapshotDTO.id, snapshotDTO.creationDate, snapshotDTO.description, snapshotDTO.release)
  );
};

const createById = async (dataSetId, description) => {
  return await apiSnapshot.createById(dataSetId, description);
};

const deleteById = async (dataSetId, snapshotId) => {
  const dataDeleted = await apiSnapshot.deleteById(dataSetId, snapshotId);
  return dataDeleted;
};

const restoreById = async (dataflowId, dataSetId, snapshotId) => {
  return await apiSnapshot.restoreById(dataflowId, dataSetId, snapshotId);
};

const releaseById = async (dataflowId, dataSetId, snapshotId) => {
  return await apiSnapshot.releaseById(dataflowId, dataSetId, snapshotId);
};

export const ApiSnapshotRepository = {
  all,
  createById,
  deleteById,
  restoreById,
  releaseById
};
