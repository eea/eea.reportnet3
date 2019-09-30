export const Upload = ({ documentRepository }) => async (dataflowId, description, language, file) =>
  documentRepository.uploadDocument(dataflowId, description, language, file);
