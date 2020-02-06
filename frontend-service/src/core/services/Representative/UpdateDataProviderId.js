export const UpdateDataProviderId = ({ representativeRepository }) => async (representativeId, dataProviderId) =>
  representativeRepository.updateDataProviderId(representativeId, dataProviderId);
