export const Delete = ({ webLinkRepository }) => async weblinkToDelete =>
  webLinkRepository.deleteWeblink(weblinkToDelete);
