export const DownloadById = ({ documentRepository }) => async documentId =>
  documentRepository.downloadDocumentById(documentId);
