import { snapshotRepository } from 'repositories/SnapshotRepository';
import { Snapshot } from 'entities/Snapshot';

const allDesigner = async datasetSchemaId => parseSnapshotList(await snapshotRepository.allDesigner(datasetSchemaId));

const createByIdDesigner = async (datasetId, datasetSchemaId, description) => {
  return await snapshotRepository.createByIdDesigner(datasetId, datasetSchemaId, description);
};

const deleteByIdDesigner = async (datasetSchemaId, snapshotId) => {
  return await snapshotRepository.deleteByIdDesigner(datasetSchemaId, snapshotId);
};

const restoreByIdDesigner = async (datasetSchemaId, snapshotId) => {
  return await snapshotRepository.restoreByIdDesigner(datasetSchemaId, snapshotId);
};

const allReporter = async datasetId => parseSnapshotList(await snapshotRepository.allReporter(datasetId));

const createByIdReporter = async (datasetId, description, isReleased) => {
  return await snapshotRepository.createByIdReporter(datasetId, description, isReleased);
};

const deleteByIdReporter = async (datasetId, snapshotId) => {
  return await snapshotRepository.deleteByIdReporter(datasetId, snapshotId);
};

const restoreByIdReporter = async (dataflowId, datasetId, snapshotId) => {
  return await snapshotRepository.restoreByIdReporter(dataflowId, datasetId, snapshotId);
};

const releaseDataflow = async (dataflowId, dataProviderId, restrictFromPublic) => {
  return await snapshotRepository.releaseDataflow(dataflowId, dataProviderId, restrictFromPublic);
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

export const SnapshotService = {
  allDesigner,
  allReporter,
  createByIdDesigner,
  createByIdReporter,
  deleteByIdDesigner,
  deleteByIdReporter,
  releaseDataflow,
  restoreByIdDesigner,
  restoreByIdReporter
};
