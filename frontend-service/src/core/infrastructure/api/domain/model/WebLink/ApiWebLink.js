import { DataflowConfig } from 'conf/domain/model/Dataflow';
import { WeblinkConfig } from 'conf/domain/model/Weblink';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiWebLink = {
  all: async dataflowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/list-of-documents.json'
        : getUrl(DataflowConfig.loadDatasetsByDataflowId, {
            dataflowId: dataflowId
          }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data.weblinks;
  },
  create: async (dataflowId, weblink) => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.post({
        url: getUrl(WeblinkConfig.create, {
          dataflowId
        }),
        data: {
          description: weblink.description,
          url: weblink.url.toString().toLowerCase()
        },
        headers: {
          Authorization: `Bearer ${tokens.accessToken}`
        }
      });
      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error creating the weblink: ${error}`);
      return false;
    }
  },

  deleteWeblink: async weblinkToDelete => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.delete({
        url: getUrl(WeblinkConfig.delete, {
          weblinkId: weblinkToDelete.id
        }),
        headers: {
          Authorization: `Bearer ${tokens.accessToken}`
        }
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error deleting the weblink: ${error}`);
      return false;
    }
  },

  update: async (dataflowId, weblinkToEdit) => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.update({
        url: getUrl(WeblinkConfig.update, {
          dataflowId
        }),
        headers: {
          Authorization: `Bearer ${tokens.accessToken}`
        },
        data: {
          description: weblinkToEdit.description,
          id: weblinkToEdit.id,
          url: weblinkToEdit.url
        }
      });
      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error editing the weblink: ${error}`);
      return false;
    }
  }
};
