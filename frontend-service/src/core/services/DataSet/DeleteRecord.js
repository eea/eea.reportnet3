export const DeleteRecord = ({ dataSetRepository }) => async (dataSetId, recordId) =>
  dataSetRepository.deleteRecordById(dataSetId, recordId);
