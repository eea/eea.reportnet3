import { ApiDocumentRepository } from 'core/infrastructure/domain/model/Document/ApiDocumentRepository';

export const DocumentRepository = {
  all: () => Promise.reject('[DocumentRepository#all] must be implemented')
};

export const documentRepository = Object.assign({}, DocumentRepository, ApiDocumentRepository);
