import { DataflowConfig } from './config/DataflowConfig';
import { DocumentConfig } from './config/DocumentConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const DocumentRepository = {
  getAll: async dataflowId => {
    return await HTTPRequester.get({ url: getUrl(DataflowConfig.get, { dataflowId }) });
  },

  delete: async documentId => {
    return await HTTPRequester.delete({ url: getUrl(DocumentConfig.delete, { documentId }) });
  },

  download: async documentId => {
    return await HTTPRequester.download({
      url: getUrl(DocumentConfig.download, { documentId }),
      headers: { 'Content-Type': 'application/octet-stream' }
    });
  },

  update: async (dataflowId, description, language, file, isPublic, documentId) => {
    const formData = new FormData();
    if (file?.name) {
      formData.append('file', file, file.name);
    } else {
      formData.append('file', null);
    }
    return await HTTPRequester.putWithFiles({
      url: getUrl(DocumentConfig.update, {
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
      url: getUrl(DocumentConfig.upload, {
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
