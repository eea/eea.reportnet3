import { SnapshotConfig } from './config/SnapshotConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const snapshotRepository = {
  allDesigner: async datasetSchemaId => {
    return await HTTPRequester.get({ url: getUrl(SnapshotConfig.loadSnapshotsListDesigner, { datasetSchemaId }) });
  },

  createByIdDesigner: async (datasetId, datasetSchemaId, description) => {
    return await HTTPRequester.post({
      url: getUrl(SnapshotConfig.createSnapshotDesigner, { datasetId, datasetSchemaId, description }),
      data: { description }
    });
  },

  deleteByIdDesigner: async (datasetSchemaId, snapshotId) => {
    return await HTTPRequester.delete({
      url: getUrl(SnapshotConfig.deleteSnapshotByIdDesigner, { datasetSchemaId, snapshotId })
    });
  },

  restoreByIdDesigner: async (datasetSchemaId, snapshotId) => {
    return await HTTPRequester.post({
      url: getUrl(SnapshotConfig.restoreSnapshotDesigner, { datasetSchemaId, snapshotId }),
      data: { snapshotId }
    });
  },

  allReporter: async datasetId => {
    return await HTTPRequester.get({ url: getUrl(SnapshotConfig.loadSnapshotsListReporter, { datasetId }) });
  },

  createByIdReporter: async (datasetId, description, isReleased) => {
    return await HTTPRequester.post({
      url: getUrl(SnapshotConfig.createSnapshotReporter, { datasetId }),
      data: { description, released: isReleased }
    });
  },

  deleteByIdReporter: async (datasetId, snapshotId) => {
    return await HTTPRequester.delete({
      url: getUrl(SnapshotConfig.deleteSnapshotByIdReporter, { datasetId, snapshotId })
    });
  },

  restoreByIdReporter: async (dataflowId, datasetId, snapshotId) => {
    return await HTTPRequester.post({
      url: getUrl(SnapshotConfig.restoreSnapshotReporter, { dataflowId, datasetId, snapshotId }),
      data: { snapshotId }
    });
  },

  releaseDataflow: async (dataflowId, dataProviderId, restrictFromPublic) => {
    return await HTTPRequester.post({
      url: getUrl(SnapshotConfig.releaseDataflow, { dataflowId, dataProviderId, restrictFromPublic })
    });
  }
};
