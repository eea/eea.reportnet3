import { SnapshotRepository } from 'repositories/SnapshotRepository';
import { Snapshot } from 'entities/Snapshot';

const allDesigner = async datasetSchemaId => parseSnapshotList(await SnapshotRepository.allDesigner(datasetSchemaId));

const createByIdDesigner = async (datasetId, datasetSchemaId, description) => {
  return await SnapshotRepository.createByIdDesigner(datasetId, datasetSchemaId, description);
};

const deleteByIdDesigner = async (datasetSchemaId, snapshotId) => {
  return await SnapshotRepository.deleteByIdDesigner(datasetSchemaId, snapshotId);
};

const restoreByIdDesigner = async (datasetSchemaId, snapshotId) => {
  return await SnapshotRepository.restoreByIdDesigner(datasetSchemaId, snapshotId);
};

const allReporter = async datasetId => parseSnapshotList(await SnapshotRepository.allReporter(datasetId));

const createByIdReporter = async (datasetId, description, isReleased) => {
  return await SnapshotRepository.createByIdReporter(datasetId, description, isReleased);
};

const deleteByIdReporter = async (datasetId, snapshotId) => {
  return await SnapshotRepository.deleteByIdReporter(datasetId, snapshotId);
};

const restoreByIdReporter = async (dataflowId, datasetId, snapshotId) => {
  return await SnapshotRepository.restoreByIdReporter(dataflowId, datasetId, snapshotId);
};

const releaseDataflow = async (dataflowId, dataProviderId, restrictFromPublic) => {
  return await SnapshotRepository.releaseDataflow(dataflowId, dataProviderId, restrictFromPublic);
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
