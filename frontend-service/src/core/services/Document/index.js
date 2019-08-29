import { documentRepository } from 'core/domain/model/Document/DocumentRepository';
import { GetAll } from './GetAll';
import { DownloadById } from './DownloadById';
import { Upload } from './Upload';

export const DocumentService = {
  all: GetAll({ documentRepository }),
  downloadDocumentById: DownloadById({ documentRepository }),
  uploadDocument: Upload({ documentRepository })
};
