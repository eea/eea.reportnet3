import { DocumentRepository } from 'repositories/DocumentRepository';
import { Document } from 'entities/Document';

import { config } from 'conf/index';

export const DocumentService = {
  getAll: async dataflowId => {
    const response = await DocumentRepository.getAll(dataflowId);
    return response.data.documents.map(
      documentDTO =>
        new Document({
          category: documentDTO.category,
          date: documentDTO.date,
          description: documentDTO.description,
          id: documentDTO.id,
          isPublic: documentDTO.isPublic,
          language: config.languages.filter(language => language.code === documentDTO.language).map(name => name.name),
          size: documentDTO.size,
          title: documentDTO.name,
          url: documentDTO.url
        })
    );
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
