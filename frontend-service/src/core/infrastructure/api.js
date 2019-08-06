import { config } from 'assets/conf';
import { getUrl } from 'core/infrastructure/getUrl';
import { HTTPRequester } from './HTTPRequester';

export const api = {
  dataflows: async () => {
    const response = await HTTPRequester.get('/characters.json');
    return response.json();
  },
  dataSetErrorsById: async (dataSetId, pageNum, pageSize, sortField, asc) => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/list-of-errors.json'
        : getUrl(config.loadStatisticsAPI.url, {
            dataSetId: dataSetId,
            pageNum: pageNum,
            pageSize: pageSize,
            sortField: sortField,
            asc: asc
          }),
      queryString: {}
    });
    return response.data;
  },
  errorPositionByObjectId: async (objectId, dataSetId, entityType) => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/response_getTableFromAnyObjectId.json'
        : getUrl(config.validationViewerAPI.url, {
            objectId: objectId,
            dataSetId: dataSetId,
            entityType: entityType
          }),
      queryString: {}
    });
    return response.data;
  },
  documents: async url => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON ? '/jsons/list-of-documents.json' : url,
      queryString: {}
    });
    return response.data.documents;
  },
  downloadDocumentById: async documentId => {
    await HTTPRequester.get({
      url: getUrl(config.downloadDocumentByIdAPI.url, {
        documentId: documentId
      }),
      queryString: {}
    });
  },
  snapshots: async url => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON ? '/jsons/snapshots.json' : url,
      queryString: {}
    });
    return response.data;
  },
  webLinks: async url => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON ? '/jsons/list-of-documents.json' : url,
      queryString: {}
    });
    return response.data.weblinks;
  }
};
