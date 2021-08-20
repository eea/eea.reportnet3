export const FeedbackConfig = {
  createMessage: '/collaboration/createMessage/dataflow/{:dataflowId}',
  importFile: '/collaboration/createMessage/dataflow/{:dataflowId}/attachment?providerId={:providerId}',
  getAllMessages: '/collaboration/findMessages/dataflow/{:dataflowId}?page={:page}&providerId={:providerId}',
  getMessageAttachment: '/findMessages/dataflow/{:dataflowId}/getMessageAttachment',
  markMessagesAsRead: '/collaboration/updateMessageReadStatus/dataflow/{:dataflowId}'
};
