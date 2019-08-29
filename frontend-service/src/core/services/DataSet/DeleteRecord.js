export const DeleteRecord = ({ dataSetRepository }) => async (dataSetId, recordIds) =>
  dataSetRepository.deleteRecordByIds(dataSetId, recordIds);
