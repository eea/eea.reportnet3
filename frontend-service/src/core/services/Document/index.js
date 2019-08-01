import { documentRepository } from 'core/domain/model/Document/DocumentRepository';
import { GetAllDocuments } from './GetAllDocuments';
import { GetDocumentById } from './GetDocumentById';

export const DocumentService = {
  all: GetAllDocuments({ documentRepository }),
  getByDocumentId: GetDocumentById({ documentRepository })
};
