export const GetDocumentById = ({ documentRepository }) => async documentId =>
  documentRepository.getDocumentById(documentId);
