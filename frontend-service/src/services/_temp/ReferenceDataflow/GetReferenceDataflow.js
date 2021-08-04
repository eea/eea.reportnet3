export const GetReferenceDataflow = ({ referenceDataflowRepository }) => async referenceDataflowId =>
  referenceDataflowRepository.referenceDataflow(referenceDataflowId);
