import { api } from 'core/infrastructure/api';
import { Snapshot } from 'core/domain/model/Snapshot/Snapshot';

const all = async dataSetId => {
  const snapshotsDTO = await api.snapshots(dataSetId);
  return snapshotsDTO.map(
    snapshotDTO => new Snapshot(snapshotDTO.id, snapshotDTO.creationDate, snapshotDTO.description)
  );
};

const createById = async (dataSetId, description) => {
  return await api.createSnapshotById(dataSetId, description);
};

const deleteById = async (dataSetId, snapshotId) => {
  const dataDeleted = await api.deleteSnapshotById(dataSetId, snapshotId);
  return dataDeleted;
};

const restoreById = async (dataFlowId, dataSetId, snapshotId) => {
  return await api.retoreSnapshotById(dataFlowId, dataSetId, snapshotId);
};

export const ApiSnapshotRepository = {
  all,
  createById,
  deleteById,
  restoreById
};
