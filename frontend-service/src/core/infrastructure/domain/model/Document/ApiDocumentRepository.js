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
  const fileData = await api.downloadDocumentById(documentId);
  return fileData;
};

const uploadDocument = async (dataFlowId, description, language, file) => {
  const responseData = await api.uploadDocument(dataFlowId, description, language, file);
  return responseData;
};

export const ApiDocumentRepository = {
  all,
  downloadDocumentById,
  uploadDocument
};
