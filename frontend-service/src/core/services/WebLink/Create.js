export const Create = ({ webLinkRepository }) => async (dataflowId, weblinkToCreate) =>
  webLinkRepository.create(dataflowId, weblinkToCreate);
