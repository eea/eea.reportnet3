export const GetReferencingDataflows = ({ referenceDataflowRepository }) => async referenceDataflowId =>
  referenceDataflowRepository.getReferencingDataflows(referenceDataflowId);
