import { apiSnapshot } from 'core/infrastructure/api/domain/model/Snapshot/ApiSnapshot';
import { Snapshot } from 'core/domain/model/Snapshot/Snapshot';

const allDesigner = async datasetSchemaId => {
  const snapshotsDTO = await apiSnapshot.allDesigner(datasetSchemaId);

  const snapshotsList = snapshotsDTO
    ? snapshotsDTO.map(
        snapshotDTO =>
          new Snapshot(snapshotDTO.id, snapshotDTO.creationDate, snapshotDTO.description, snapshotDTO.release)
      )
    : [];

  return snapshotsList;
};

const createByIdDesigner = async (datasetId, datasetSchemaId, description) => {
  return await apiSnapshot.createByIdDesigner(datasetId, datasetSchemaId, description);
};

const deleteByIdDesigner = async (datasetSchemaId, snapshotId) => {
  return await apiSnapshot.deleteByIdDesigner(datasetSchemaId, snapshotId);
};

const restoreByIdDesigner = async (datasetSchemaId, snapshotId) => {
  return await apiSnapshot.restoreByIdDesigner(datasetSchemaId, snapshotId);
};

const releaseByIdDesigner = async (datasetSchemaId, snapshotId) => {
  return await apiSnapshot.releaseByIdDesigner(datasetSchemaId, snapshotId);
};

const allReporter = async datasetId => {
  const snapshotsDTO = await apiSnapshot.allReporter(datasetId);

  return snapshotsDTO.map(
    snapshotDTO =>
      new Snapshot(
        snapshotDTO.id,
        snapshotDTO.creationDate,
        snapshotDTO.description,
        snapshotDTO.release,
        undefined,
        undefined,
        undefined,
        snapshotDTO.isValid
      )
  );
};

const createByIdReporter = async (datasetId, description, isReleased) => {
  return await apiSnapshot.createByIdReporter(datasetId, description, isReleased);
};

const deleteByIdReporter = async (datasetId, snapshotId) => {
  return await apiSnapshot.deleteByIdReporter(datasetId, snapshotId);
};

const restoreByIdReporter = async (dataflowId, datasetId, snapshotId) => {
  return await apiSnapshot.restoreByIdReporter(dataflowId, datasetId, snapshotId);
};

const releaseByIdReporter = async (dataflowId, datasetId, snapshotId) => {
  const isReleased = await apiSnapshot.releaseByIdReporter(dataflowId, datasetId, snapshotId);

  const snapshotToRelease = new Snapshot();
  snapshotToRelease.id = snapshotId;
  snapshotToRelease.isReleased = isReleased;

  return snapshotToRelease;
};

export const ApiSnapshotRepository = {
  allDesigner,
  createByIdDesigner,
  deleteByIdDesigner,
  restoreByIdDesigner,
  releaseByIdDesigner,

  allReporter,
  createByIdReporter,
  deleteByIdReporter,
  restoreByIdReporter,
  releaseByIdReporter
};
