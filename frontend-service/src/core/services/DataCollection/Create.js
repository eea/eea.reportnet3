export const Create = ({ dataCollectionRepository }) => async (dataflowId, endDate, isManualTechnicalAcceptance) =>
  dataCollectionRepository.create(dataflowId, endDate, isManualTechnicalAcceptance);
