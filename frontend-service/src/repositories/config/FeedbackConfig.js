export const FeedbackConfig = {
  create: '/collaboration/createMessage/dataflow/{:dataflowId}',
  importFile: '/collaboration/import',
  loadMessages: '/collaboration/findMessages/dataflow/{:dataflowId}?page={:page}&providerId={:providerId}',
  markAsRead: '/collaboration/updateMessageReadStatus/dataflow/{:dataflowId}'
};
