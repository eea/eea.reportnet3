export const UploadDocument = ({ documentRepository }) => async (dataFlowId, description, language, file) =>
  documentRepository.uploadDocument(dataFlowId, description, language, file);
