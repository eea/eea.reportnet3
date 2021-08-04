export const DeleteDataflow = ({ referenceDataflowRepository }) => async referenceDataflowId =>
  referenceDataflowRepository.deleteReferenceDataflow(referenceDataflowId);
