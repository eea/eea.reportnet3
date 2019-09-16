export const DeleteDocument = ({ documentRepository }) => async documentId =>
  documentRepository.deleteDocument(documentId);
