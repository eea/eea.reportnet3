export const CreateReference = ({ dataCollectionRepository }) => async (dataflowId, stopAndNotifyPKError) =>
  dataCollectionRepository.createReference(dataflowId, stopAndNotifyPKError);
