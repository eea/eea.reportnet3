import { DeleteDocument } from './DeleteDocument';
import { DownloadById } from './DownloadById';
import { EditDocument } from './EditDocument';
import { GetAll } from './GetAll';
import { Upload } from './Upload';

import { documentRepository } from 'entities/Document/DocumentRepository';

export const DocumentService = {
  all: GetAll({ documentRepository }),
  deleteDocument: DeleteDocument({ documentRepository }),
  downloadDocumentById: DownloadById({ documentRepository }),
  editDocument: EditDocument({ documentRepository }),
  uploadDocument: Upload({ documentRepository })
};
