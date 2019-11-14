export const Update = ({ webLinkRepository }) => async (dataflowId, weblinkToUpdate) =>
  webLinkRepository.update(dataflowId, weblinkToUpdate);
