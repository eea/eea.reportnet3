export const DownloadTemplate = ({ representativeRepository }) => async dataProviderGroupId =>
  representativeRepository.downloadTemplateById(dataProviderGroupId);
