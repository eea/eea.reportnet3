import { DocumentRepository } from 'repositories/DocumentRepository';

import { DocumentUtils } from 'services/_utils/DocumentUtils';

export const DocumentService = {
  getAll: async dataflowId => {
    const response = await DocumentRepository.getAll(dataflowId);
    return DocumentUtils.parseDocumentListDTO(response.data);
  },

  download: async (documentId, dataflowId) => await DocumentRepository.download(documentId, dataflowId),

  publicDownload: async (documentId, dataflowId) => await DocumentRepository.publicDownload(documentId, dataflowId),

  upload: async (dataflowId, description, language, file, isPublic) =>
    await DocumentRepository.upload(dataflowId, description, language, file, isPublic),

  update: async (dataflowId, description, language, file, isPublic, documentId) =>
    await DocumentRepository.update(dataflowId, description, language, file, isPublic, documentId),

  delete: async (documentId, dataflowId) => await DocumentRepository.delete(documentId, dataflowId)
};
