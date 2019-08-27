export const DownloadDocumentById = ({ documentRepository }) => async documentId =>
  documentRepository.downloadDocumentById(documentId);
