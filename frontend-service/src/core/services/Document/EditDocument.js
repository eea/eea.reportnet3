export const EditDocument = ({ documentRepository }) => async (dataflowId, description, language, file, isPublic) =>
  documentRepository.editDocument(dataflowId, description, language, file, isPublic);
