export const CreateReference = ({ dataCollectionRepository }) => async dataflowId =>
  dataCollectionRepository.createReference(dataflowId);
