import { DataflowConfig } from 'conf/domain/model/Dataflow';
import { DocumentConfig } from 'conf/domain/model/Document';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiDocument = {
  all: async dataflowId => {
    const response = await HTTPRequester.get({ url: getUrl(DataflowConfig.loadDatasetsByDataflowId, { dataflowId }) });
    return response.data.documents;
  },

  deleteDocument: async documentId => {
    const response = await HTTPRequester.delete({ url: getUrl(DocumentConfig.deleteDocument, { documentId }) });
    return response.status;
  },

  downloadById: async documentId => {
    const response = await HTTPRequester.download({
      url: getUrl(DocumentConfig.downloadDocumentById, { documentId }),
      headers: { 'Content-Type': 'application/octet-stream' }
    });
    return response.data;
  },

  editDocument: async (dataflowId, description, language, file, isPublic, documentId) => {
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
        dataflowId,
        description: encodeURIComponent(description),
        language,
        isPublic,
        documentId
      }),
      data: formData,
      headers: { 'Content-Type': undefined }
    });
    return response.status;
  },

  upload: async (dataflowId, description, language, file, isPublic) => {
    const formData = new FormData();
    formData.append('file', file, file.name);
    const response = await HTTPRequester.postWithFiles({
      url: getUrl(DocumentConfig.uploadDocument, {
        dataflowId,
        description: encodeURIComponent(description),
        language,
        isPublic
      }),
      data: formData,
      headers: { 'Content-Type': undefined }
    });
    return response.status;
  }
};
