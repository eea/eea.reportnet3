export const DownloadFile = ({ validationRepository }) => async (datasetId, fileName) =>
  validationRepository.downloadFile(datasetId, fileName);
