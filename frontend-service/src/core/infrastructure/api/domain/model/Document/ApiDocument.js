import { DataflowConfig } from 'conf/domain/model/Dataflow';
import { DocumentConfig } from 'conf/domain/model/Document';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiDocument = {
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
    return response.data.documents;
  },
  downloadById: async documentId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.download({
      url: window.env.REACT_APP_JSON
        ? '/jsons/list-of-documents.json'
        : getUrl(DocumentConfig.downloadDocumentById, {
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
  upload: async (dataflowId, description, language, file, isPublic) => {
    const tokens = userStorage.get();
    const formData = new FormData();
    formData.append('file', file, file.name);
    const response = await HTTPRequester.postWithFiles({
      url: getUrl(DocumentConfig.uploadDocument, {
        dataflowId: dataflowId,
        description: encodeURIComponent(description),
        language: language,
        isPublic: isPublic
      }),
      queryString: {},
      data: formData,
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`,
        'Content-Type': undefined
      }
    });
    return response.status;
  },
  editDocument: async (dataflowId, description, language, file, isPublic, documentId) => {
    const tokens = userStorage.get();
    const formData = new FormData();
    const isEmpty = arg => {
      for (var item in arg) {
        return false;
      }
      return true;
    };

    if (isEmpty(file)) {
      formData.append('file', null);
    } else {
      formData.append('file', file, file.name);
    }

    const response = await HTTPRequester.putWithFiles({
      url: getUrl(DocumentConfig.editDocument, {
        dataflowId: dataflowId,
        description: encodeURIComponent(description),
        language: language,
        isPublic: isPublic,
        documentId: documentId
      }),
      queryString: {},
      data: formData,
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`,
        'Content-Type': undefined
      }
    });
    return response.status;
  },
  deleteDocument: async documentId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.delete({
      url: getUrl(DocumentConfig.deleteDocument, {
        documentId
      }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.status;
  }
};
