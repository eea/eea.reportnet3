import { ApiDocumentRepository } from 'core/infrastructure/domain/model/Document/ApiDocumentRepository';

export const DocumentRepository = {
  all: () => Promise.reject('[DocumentRepository#all] must be implemented'),
  deleteDocument: () => Promise.reject('[DocumentRepository#delete] must be implemented'),
  downloadDocumentById: () => Promise.reject('[DocumentRepository#downloadDocumentById] must be implemented'),
  editDocument: () => Promise.reject('[DocumentRepository#editDocument] must be implemented'),
  uploadDocument: () => Promise.reject('[DocumentRepository#uploadDocument] must be implemented')
};

export const documentRepository = Object.assign({}, DocumentRepository, ApiDocumentRepository);
