import { config } from 'conf';
import { getUrl } from 'core/infrastructure/api/getUrl';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiDocument = {
  all: async dataFlowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/list-of-documents.json'
        : getUrl(config.loadDataSetsByDataflowID.url, {
            dataFlowId: dataFlowId
          }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data.documents;
  },
  downloadById: async documentId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.download({
      url: window.env.REACT_APP_JSON
        ? '/jsons/list-of-documents.json'
        : getUrl(config.downloadDocumentByIdAPI.url, {
            documentId: documentId
          }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`,
        'Content-Type': 'application/octet-stream'
      }
    });
    return response.data;
  },
  upload: async (dataFlowId, description, language, file) => {
    const tokens = userStorage.get();
    const formData = new FormData();
    formData.append('file', file, file.name);
    const response = await HTTPRequester.postWithFiles({
      url: getUrl(config.uploadDocumentAPI.url, {
        dataFlowId: dataFlowId,
        description: description,
        language: language
      }),
      queryString: {},
      data: formData,
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`,
        'Content-Type': undefined
      }
    });
    return response.status;
  }
};
