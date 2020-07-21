import { SnapshotConfig } from 'conf/domain/model/Snapshot/index';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiSnapshot = {
  allDesigner: async datasetSchemaId => {
    const response = await HTTPRequester.get({
      url: getUrl(SnapshotConfig.loadSnapshotsListDesigner, {
        datasetSchemaId: datasetSchemaId
      })
    });
    return response.data;
  },
  createByIdDesigner: async (datasetId, datasetSchemaId, description) => {
    const response = await HTTPRequester.post({
      url: getUrl(SnapshotConfig.createSnapshotDesigner, {
        datasetId: datasetId,
        datasetSchemaId: datasetSchemaId,
        description: description
      }),
      data: {
        description: description
      }
    });
    return response;
  },
  deleteByIdDesigner: async (datasetSchemaId, snapshotId) => {
    const response = await HTTPRequester.delete({
      url: getUrl(SnapshotConfig.deleteSnapshotByIdDesigner, {
        datasetSchemaId: datasetSchemaId,
        snapshotId: snapshotId
      })
    });

    return response;
  },
  restoreByIdDesigner: async (datasetSchemaId, snapshotId) => {
    const response = await HTTPRequester.post({
      url: getUrl(SnapshotConfig.restoreSnapshotDesigner, {
        datasetSchemaId: datasetSchemaId,
        snapshotId: snapshotId
      }),
      data: {
        snapshotId
      }
    });

    return response.data;
  },
  releaseByIdDesigner: async (datasetSchemaId, snapshotId) => {
    const response = await HTTPRequester.update({
      url: getUrl(SnapshotConfig.releaseSnapshotDesigner, {
        datasetSchemaId: datasetSchemaId,
        snapshotId: snapshotId
      }),
      data: {
        snapshotId
      }
    });
    return response;
  },

  allReporter: async datasetId => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/snapshots.json'
        : getUrl(SnapshotConfig.loadSnapshotsListReporter, {
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
  releaseByIdReporter: async (dataflowId, datasetId, snapshotId) => {
    const response = await HTTPRequester.update({
      url: getUrl(SnapshotConfig.releaseSnapshotReporter, { dataflowId, datasetId, snapshotId }),
      data: { snapshotId }
    });
    return response.data;
  }
};
