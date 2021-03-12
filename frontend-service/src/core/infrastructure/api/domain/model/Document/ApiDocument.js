import { DataflowConfig } from 'conf/domain/model/Dataflow';
import { DocumentConfig } from 'conf/domain/model/Document';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiDocument = {
  all: async dataflowId => {
    return await HTTPRequester.get({ url: getUrl(DataflowConfig.loadDatasetsByDataflowId, { dataflowId }) });
  },

  deleteDocument: async documentId => {
    return await HTTPRequester.delete({ url: getUrl(DocumentConfig.deleteDocument, { documentId }) });
  },

  downloadById: async documentId => {
    return await HTTPRequester.download({
      url: getUrl(DocumentConfig.downloadDocumentById, { documentId }),
      headers: { 'Content-Type': 'application/octet-stream' }
    });
  },

  editDocument: async (dataflowId, description, language, file, isPublic, documentId) => {
    const formData = new FormData();
    if (file?.name) {
      formData.append('file', file, file.name);
    } else {
      formData.append('file', null);
    }
    return await HTTPRequester.putWithFiles({
      url: getUrl(DocumentConfig.editDocument, {
        dataflowId,
        description: encodeURIComponent(description),
        documentId,
        isPublic,
        language
      }),
      data: formData,
      headers: { 'Content-Type': undefined }
    });
  },

  upload: async (dataflowId, description, language, file, isPublic) => {
    const formData = new FormData();
    formData.append('file', file, file.name);

    return await HTTPRequester.postWithFiles({
      url: getUrl(DocumentConfig.uploadDocument, {
        dataflowId,
        description: encodeURIComponent(description),
        isPublic,
        language
      }),
      data: formData,
      headers: { 'Content-Type': undefined }
    });
  }
};
