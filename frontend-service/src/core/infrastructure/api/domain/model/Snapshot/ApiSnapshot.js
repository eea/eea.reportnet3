import { config } from 'conf';
import { getUrl } from 'core/infrastructure/api/getUrl';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiSnapshot = {
  createById: async (datasetId, description) => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.post({
        url: getUrl(config.createSnapshot.url, {
          datasetId,
          description: description
        }),
        data: {
          description: description
        },
        headers: {
          Authorization: `Bearer ${tokens.accessToken}`
        }
      });
      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error creating the snapshot: ${error}`);
      return false;
    }
  },
  deleteById: async (datasetId, snapshotId) => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.delete({
        url: getUrl(config.deleteSnapshotByID.url, {
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
  restoreById: async (dataflowId, datasetId, snapshotId) => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.post({
        url: getUrl(config.restoreSnapshot.url, {
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
  releaseById: async (dataflowId, datasetId, snapshotId) => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.update({
        url: getUrl(config.releaseSnapshot.url, {
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
  },
  all: async datasetId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/snapshots.json'
        : getUrl(config.loadSnapshotsListAPI.url, {
            datasetId: datasetId
          }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  }
};
