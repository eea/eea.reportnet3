import { DeleteDocument } from './DeleteDocument';
import { documentRepository } from 'core/domain/model/Document/DocumentRepository';
import { DownloadById } from './DownloadById';
import { EditDocument } from './EditDocument';
import { GetAll } from './GetAll';
import { Upload } from './Upload';

export const DocumentService = {
  all: GetAll({ documentRepository }),
  downloadDocumentById: DownloadById({ documentRepository }),
  uploadDocument: Upload({ documentRepository }),
  deleteDocument: DeleteDocument({ documentRepository }),
  editDocument: EditDocument({ documentRepository })
};
