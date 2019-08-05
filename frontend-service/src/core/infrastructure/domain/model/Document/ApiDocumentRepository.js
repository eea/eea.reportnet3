import { api } from 'core/infrastructure/api';
import { Document } from 'core/domain/model/Document/Document';

const all = async url => {
  const documentsDTO = await api.documents(url);

  return documentsDTO.map(
    documentDTO =>
      new Document(
        documentDTO.id,
        documentDTO.name,
        documentDTO.description,
        documentDTO.category,
        documentDTO.language,
        documentDTO.url
      )
  );
};

const downloadDocumentById = async documentId => {
  return await api.downloadDocumentById(documentId);
};

export const ApiDocumentRepository = {
  all,
  downloadDocumentById
};
