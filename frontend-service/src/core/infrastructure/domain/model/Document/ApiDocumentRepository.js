import { apiDocument } from 'core/infrastructure/api/domain/model/Document';
import { Document } from 'core/domain/model/Document/Document';

import { config } from 'conf/index';

const all = async dataflowId => {
  const response = await apiDocument.all(dataflowId);
  response.data.documents = response.data.documents.map(
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

  return response;
};

const downloadDocumentById = async documentId => await apiDocument.downloadById(documentId);

const uploadDocument = async (dataflowId, description, language, file, isPublic) => {
  return await apiDocument.upload(dataflowId, description, language, file, isPublic);
};

const editDocument = async (dataflowId, description, language, file, isPublic, documentId) => {
  return await apiDocument.editDocument(dataflowId, description, language, file, isPublic, documentId);
};

const deleteDocument = async documentId => await apiDocument.deleteDocument(documentId);

const getCountryName = countryCode => {
  return config.languages.filter(language => language.code === countryCode).map(name => name.name);
};

export const ApiDocumentRepository = { all, deleteDocument, downloadDocumentById, editDocument, uploadDocument };
