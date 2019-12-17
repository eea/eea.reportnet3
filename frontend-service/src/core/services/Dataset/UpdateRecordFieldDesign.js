export const UpdateRecordFieldDesign = ({ datasetRepository }) => async (datasetId, record) =>
  datasetRepository.updateRecordFieldDesign(datasetId, record);
