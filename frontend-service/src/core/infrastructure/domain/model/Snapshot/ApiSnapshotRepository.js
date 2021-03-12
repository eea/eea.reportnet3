import { apiSnapshot } from 'core/infrastructure/api/domain/model/Snapshot/ApiSnapshot';
import { Snapshot } from 'core/domain/model/Snapshot/Snapshot';

const allDesigner = async datasetSchemaId => parseSnapshotList(await apiSnapshot.allDesigner(datasetSchemaId));

const createByIdDesigner = async (datasetId, datasetSchemaId, description) => {
  return await apiSnapshot.createByIdDesigner(datasetId, datasetSchemaId, description);
};

const deleteByIdDesigner = async (datasetSchemaId, snapshotId) => {
  return await apiSnapshot.deleteByIdDesigner(datasetSchemaId, snapshotId);
};

const restoreByIdDesigner = async (datasetSchemaId, snapshotId) => {
  return await apiSnapshot.restoreByIdDesigner(datasetSchemaId, snapshotId);
};

const allReporter = async datasetId => parseSnapshotList(await apiSnapshot.allReporter(datasetId));

const createByIdReporter = async (datasetId, description, isReleased) => {
  return await apiSnapshot.createByIdReporter(datasetId, description, isReleased);
};

const deleteByIdReporter = async (datasetId, snapshotId) => {
  return await apiSnapshot.deleteByIdReporter(datasetId, snapshotId);
};

const restoreByIdReporter = async (dataflowId, datasetId, snapshotId) => {
  return await apiSnapshot.restoreByIdReporter(dataflowId, datasetId, snapshotId);
};

const releaseDataflow = async (dataflowId, dataProviderId, restrictFromPublic) => {
  return await apiSnapshot.releaseDataflow(dataflowId, dataProviderId, restrictFromPublic);
};

const parseSnapshotList = response => {
  response.data = response.data.map(
    snapshotDTO =>
      new Snapshot({
        creationDate: snapshotDTO.creationDate,
        description: snapshotDTO.description,
        id: snapshotDTO.id,
        isAutomatic: snapshotDTO.automatic,
        isReleased: snapshotDTO.release
      })
  );

  return response;
};

export const ApiSnapshotRepository = {
  allDesigner,
  createByIdDesigner,
  deleteByIdDesigner,
  restoreByIdDesigner,

  allReporter,
  createByIdReporter,
  deleteByIdReporter,
  restoreByIdReporter,

  releaseDataflow
};
