export const EditDocument = ({ documentRepository }) => async (
  dataflowId,
  description,
  language,
  file,
  isPublic,
  documentId
) => documentRepository.editDocument(dataflowId, description, language, file, isPublic, documentId);
