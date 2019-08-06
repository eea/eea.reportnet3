export const UploadDocument = ({ documentRepository }) => async (dataFlowId, title, description, language, file) =>
  documentRepository.uploadDocument(dataFlowId, title, description, language, file);
