export const AddRecord = ({ dataSetRepository }) => async (dataSetId, tableSchemaId, record) =>
  dataSetRepository.addRecordById(dataSetId, tableSchemaId, record);
