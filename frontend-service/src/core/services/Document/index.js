import { documentRepository } from 'core/domain/model/Document/DocumentRepository';
import { GetAll } from './GetAll';
import { DownloadById } from './DownloadById';
import { Upload } from './Upload';
import { DeleteDocument } from './DeleteDocument';

export const DocumentService = {
  all: GetAll({ documentRepository }),
  downloadDocumentById: DownloadById({ documentRepository }),
  uploadDocument: Upload({ documentRepository }),
  deleteDocument: DeleteDocument({ documentRepository })
};
