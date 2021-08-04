import { documentRepository } from 'repositories/DocumentRepository';
import { Document } from 'entities/Document';

import { config } from 'conf/index';

const all = async dataflowId => {
  const response = await documentRepository.all(dataflowId);
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

const downloadDocumentById = async documentId => await documentRepository.downloadById(documentId);

const uploadDocument = async (dataflowId, description, language, file, isPublic) => {
  return await documentRepository.upload(dataflowId, description, language, file, isPublic);
};

const editDocument = async (dataflowId, description, language, file, isPublic, documentId) => {
  return await documentRepository.editDocument(dataflowId, description, language, file, isPublic, documentId);
};

const deleteDocument = async documentId => await documentRepository.deleteDocument(documentId);

const getCountryName = countryCode => {
  return config.languages.filter(language => language.code === countryCode).map(name => name.name);
};

export const DocumentService = { all, deleteDocument, downloadDocumentById, editDocument, uploadDocument };
