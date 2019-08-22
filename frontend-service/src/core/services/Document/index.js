import { documentRepository } from 'core/domain/model/Document/DocumentRepository';
import { GetAllDocuments } from './GetAllDocuments';
import { DownloadDocumentById } from './DownloadDocumentById';
import { UploadDocument } from './UploadDocument';

export const DocumentService = {
  all: GetAllDocuments({ documentRepository }),
  downloadDocumentById: DownloadDocumentById({ documentRepository }),
  uploadDocument: UploadDocument({ documentRepository })
};
