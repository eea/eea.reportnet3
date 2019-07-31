import { api } from 'core/infrastructure/api';
import { Document } from 'core/domain/model/Document/Document';

const all = async () => {
  const documentsDTO = await api.documents();

  return documentsDTO.map(
    documentDTO =>
      new Document(
        documentDTO.title,
        documentDTO.description,
        documentDTO.category,
        documentDTO.language,
        documentDTO.url
      )
  );
};

export const ApiDocumentRepository = {
  all
};
