export const GetAllDocuments = ({ documentRepository }) => async url => documentRepository.all(url);
