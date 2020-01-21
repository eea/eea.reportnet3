export const Upload = ({ documentRepository }) => async (dataflowId, description, language, file, isPublic) =>
  documentRepository.uploadDocument(dataflowId, description, language, file, isPublic);
