import { SnapshotConfig } from 'conf/domain/model/Snapshot/index';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiSnapshot = {
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
    const response = await HTTPRequester.get({
      url: getUrl(SnapshotConfig.loadSnapshotsListReporter, {
        datasetId: datasetId
      })
    });
    return response.data;
  },

  createByIdReporter: async (datasetId, description, isReleased) => {
    const response = await HTTPRequester.post({
      url: getUrl(SnapshotConfig.createSnapshotReporter, {
        datasetId
      }),
      data: {
        description,
        released: isReleased
      }
    });
    return response.data;
  },

  deleteByIdReporter: async (datasetId, snapshotId) => {
    const response = await HTTPRequester.delete({
      url: getUrl(SnapshotConfig.deleteSnapshotByIdReporter, {
        datasetId,
        snapshotId: snapshotId
      })
    });
    return response.data;
  },

  restoreByIdReporter: async (dataflowId, datasetId, snapshotId) => {
    const response = await HTTPRequester.post({
      url: getUrl(SnapshotConfig.restoreSnapshotReporter, {
        dataflowId,
        datasetId,
        snapshotId
      }),
      data: { snapshotId }
    });
    return response.data;
  },

  releaseDataflow: async (dataflowId, dataProviderId, restrictFromPublic) => {
    const response = await HTTPRequester.post({
      url: getUrl(SnapshotConfig.releaseDataflow, { dataflowId, dataProviderId, restrictFromPublic })
    });
    return response.data;
  }
};
