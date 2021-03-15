export const Delete = ({ webLinkRepository }) => async weblinkToDelete =>
  webLinkRepository.deleteWebLink(weblinkToDelete);
