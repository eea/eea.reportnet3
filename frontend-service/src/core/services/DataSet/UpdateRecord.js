export const UpdateRecord = ({ dataSetRepository }) => async (dataSetId, record) =>
  dataSetRepository.updateRecordsById(dataSetId, record);
