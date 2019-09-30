import { apiDocument } from 'core/infrastructure/api/domain/model/Document';
import { Document } from 'core/domain/model/Document/Document';
import { async } from 'q';

const all = async dataflowId => {
  const documentsDTO = await apiDocument.all(dataflowId);

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
  const fileData = await apiDocument.downloadById(documentId);
  return fileData;
};

const uploadDocument = async (dataflowId, description, language, file) => {
  const responseData = await apiDocument.upload(dataflowId, description, language, file);
  return responseData;
};

const deleteDocument = async documentId => {
  const responseData = await apiDocument.deleteDocument(documentId);
  return responseData;
};

export const ApiDocumentRepository = {
  all,
  downloadDocumentById,
  uploadDocument,
  deleteDocument
};
