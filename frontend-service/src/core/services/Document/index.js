import { GetAllDocuments } from './GetAllDocuments';
import { documentRepository } from 'core/domain/model/Document/DocumentRepository';

export const DocumentService = {
  all: GetAllDocuments({ documentRepository })
};
