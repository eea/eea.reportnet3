import { SnapshotConfig } from 'conf/domain/model/Snapshot/index';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiSnapshot = {
  allDesigner: async datasetSchemaId => {
    const tokens = userStorage.get();

    const response = await HTTPRequester.get({
      url: getUrl(SnapshotConfig.loadSnapshotsListDesigner, {
        datasetSchemaId: datasetSchemaId
      }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  createByIdDesigner: async (datasetId, datasetSchemaId, description) => {
    const tokens = userStorage.get();

    const response = await HTTPRequester.post({
      url: getUrl(SnapshotConfig.createSnapshotDesigner, {
        datasetId: datasetId,
        datasetSchemaId: datasetSchemaId,
        description: description
      }),
      data: {
        description: description
      },
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  },
  deleteByIdDesigner: async (datasetSchemaId, snapshotId) => {
    const tokens = userStorage.get();

    const response = await HTTPRequester.delete({
      url: getUrl(SnapshotConfig.deleteSnapshotByIdDesigner, {
        datasetSchemaId: datasetSchemaId,
        snapshotId: snapshotId
      }),
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });

    return response;
  },
  restoreByIdDesigner: async (datasetSchemaId, snapshotId) => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.post({
        url: getUrl(SnapshotConfig.restoreSnapshotDesigner, {
          datasetSchemaId: datasetSchemaId,
          snapshotId: snapshotId
        }),
        headers: {
          Authorization: `Bearer ${tokens.accessToken}`
        },
        data: {
          snapshotId
        }
      });
      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error restoring the snapshot: ${error}`);
      return false;
    }
  },
  releaseByIdDesigner: async (datasetSchemaId, snapshotId) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.update({
      url: getUrl(SnapshotConfig.releaseSnapshotDesigner, {
        datasetSchemaId: datasetSchemaId,
        snapshotId: snapshotId
      }),
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      },
      data: {
        snapshotId
      }
    });
    return response;
  },

  allReporter: async datasetId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/snapshots.json'
        : getUrl(SnapshotConfig.loadSnapshotsListReporter, {
            datasetId: datasetId
          }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  createByIdReporter: async (datasetId, description, isRelease) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.post({
      url: getUrl(SnapshotConfig.createSnapshotReporter, {
        datasetId
      }),
      data: {
        description,
        released: isRelease
      },
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  },
  deleteByIdReporter: async (datasetId, snapshotId) => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.delete({
        url: getUrl(SnapshotConfig.deleteSnapshotByIdReporter, {
          datasetId,
          snapshotId: snapshotId
        }),
        headers: {
          Authorization: `Bearer ${tokens.accessToken}`
        }
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error deleting snapshot data: ${error}`);
      return false;
    }
  },
  restoreByIdReporter: async (dataflowId, datasetId, snapshotId) => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.post({
        url: getUrl(SnapshotConfig.restoreSnapshotReporter, {
          dataflowId,
          datasetId,
          snapshotId
        }),
        headers: {
          Authorization: `Bearer ${tokens.accessToken}`
        },
        data: {
          snapshotId
        }
      });
      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error restoring the snapshot: ${error}`);
      return false;
    }
  },
  releaseByIdReporter: async (dataflowId, datasetId, snapshotId) => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.update({
        url: getUrl(SnapshotConfig.releaseSnapshotReporter, {
          dataflowId,
          datasetId,
          snapshotId
        }),
        headers: {
          Authorization: `Bearer ${tokens.accessToken}`
        },
        data: {
          snapshotId
        }
      });
      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error releasing the snapshot: ${error}`);
      return false;
    }
  }
};
