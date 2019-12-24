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
  const isRestored = await apiSnapshot.restoreByIdDesigner(datasetSchemaId, snapshotId);

  const snapshotToRestore = new Snapshot();
  snapshotToRestore.id = snapshotId;
  snapshotToRestore.isRestored = isRestored;

  return snapshotToRestore;
};

const releaseByIdDesigner = async (datasetSchemaId, snapshotId) => {
  const isReleased = await apiSnapshot.releaseByIdDesigner(datasetSchemaId, snapshotId);

  const snapshotToRelease = new Snapshot();
  snapshotToRelease.id = snapshotId;
  snapshotToRelease.isReleased = isReleased;

  return snapshotToRelease;
};

const allReporter = async datasetId => {
  const snapshotsDTO = await apiSnapshot.allReporter(datasetId);

  return snapshotsDTO.map(
    snapshotDTO => new Snapshot(snapshotDTO.id, snapshotDTO.creationDate, snapshotDTO.description, snapshotDTO.release)
  );
};

const createByIdReporter = async (datasetId, description) => {
  const isCreated = await apiSnapshot.createByIdReporter(datasetId, description);

  const snapshotToCreate = new Snapshot();
  snapshotToCreate.description = description;
  snapshotToCreate.isCreated = isCreated;

  return snapshotToCreate;
};

const deleteByIdReporter = async (datasetId, snapshotId) => {
  const isDeleted = await apiSnapshot.deleteByIdReporter(datasetId, snapshotId);

  const snapshotToDelete = new Snapshot();
  snapshotToDelete.id = snapshotId;
  snapshotToDelete.isDeleted = isDeleted;

  return snapshotToDelete;
};

const restoreByIdReporter = async (dataflowId, datasetId, snapshotId) => {
  const isRestored = await apiSnapshot.restoreByIdReporter(dataflowId, datasetId, snapshotId);

  const snapshotToRestore = new Snapshot();
  snapshotToRestore.id = snapshotId;
  snapshotToRestore.isRestored = isRestored;

  return snapshotToRestore;
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
