export const GetAllUnread = ({ feedbackRepository }) => async (first, rows) =>
  feedbackRepository.allUnread(first, rows);
