import { DocumentRepository } from 'repositories/DocumentRepository';

import { DocumentUtils } from 'services/_utils/DocumentUtils';

export const DocumentService = {
  getAll: async dataflowId => {
    const response = await DocumentRepository.getAll(dataflowId);
    return DocumentUtils.parseDocumentListDTO(response.data);
  },

  getAllPublic: async dataflowId => {
    const response = await DocumentRepository.getAllPublic(dataflowId);
    return DocumentUtils.parseDocumentListDTO(response.data);
  },

  download: async documentId => await DocumentRepository.download(documentId),

  upload: async (dataflowId, description, language, file, isPublic) => {
    return await DocumentRepository.upload(dataflowId, description, language, file, isPublic);
  },

  update: async (dataflowId, description, language, file, isPublic, documentId) => {
    return await DocumentRepository.update(dataflowId, description, language, file, isPublic, documentId);
  },

  delete: async documentId => await DocumentRepository.delete(documentId)
};
