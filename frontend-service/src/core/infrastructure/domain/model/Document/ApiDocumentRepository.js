import { apiDocument } from 'core/infrastructure/api/domain/model/Document';
import { Document } from 'core/domain/model/Document/Document';

import { config } from 'conf/index';

const all = async dataflowId => {
  const documentsDTO = await apiDocument.all(dataflowId);

  return documentsDTO.map(
    documentDTO =>
      new Document({
        category: documentDTO.category,
        date: documentDTO.date,
        description: documentDTO.description,
        id: documentDTO.id,
        isPublic: documentDTO.isPublic,
        language: getCountryName(documentDTO.language),
        size: documentDTO.size,
        title: documentDTO.name,
        url: documentDTO.url
      })
  );
};

const downloadDocumentById = async documentId => {
  const fileData = await apiDocument.downloadById(documentId);
  return fileData;
};

const uploadDocument = async (dataflowId, description, language, file, isPublic) => {
  const responseData = await apiDocument.upload(dataflowId, description, language, file, isPublic);
  return responseData;
};

const editDocument = async (dataflowId, description, language, file, isPublic, documentId) => {
  const responseData = await apiDocument.editDocument(dataflowId, description, language, file, isPublic, documentId);
  return responseData;
};

const deleteDocument = async documentId => {
  const responseData = await apiDocument.deleteDocument(documentId);
  return responseData;
};

const getCountryName = countryCode => {
  return config.languages.filter(language => language.code === countryCode).map(name => name.name);
};

export const ApiDocumentRepository = { all, deleteDocument, downloadDocumentById, editDocument, uploadDocument };
