export const ExportData = ({ dataSetRepository }) => async (dataSetId, fileType) =>
  dataSetRepository.exportDataById(dataSetId, fileType);
