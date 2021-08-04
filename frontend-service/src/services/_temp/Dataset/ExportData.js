export const ExportData = ({ datasetRepository }) => async (datasetId, fileType) =>
  datasetRepository.exportDataById(datasetId, fileType);
