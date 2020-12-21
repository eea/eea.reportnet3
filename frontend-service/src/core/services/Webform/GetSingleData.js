export const GetSingleData = ({ webformRepository }) => async (datasetId, groupPaMId) => {
  return webformRepository.singlePamData(datasetId, groupPaMId);
};
