export const EditDocument = ({ documentRepository }) => async (dataflowId, description, language, file) =>
  documentRepository.editDocument(dataflowId, description, language, file);
