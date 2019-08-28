export const UpdateRecord = ({ dataSetRepository }) => async (dataSetId, recordSchemaId, record) =>
  dataSetRepository.updateRecordById(dataSetId, recordSchemaId, record);
