export const GetAllDocuments = ({ documentRepository }) => async url => documentRepository.all(url);
export const GetDocumentByDataflowIdAndDocumentId = ({ documentRepository }) => async url =>
  documentRepository.all(url);
