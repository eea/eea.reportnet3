export const DeleteRecordFieldDesign = ({ datasetRepository }) => async (datasetId, recordId) =>
  datasetRepository.deleteRecordFieldDesign(datasetId, recordId);
