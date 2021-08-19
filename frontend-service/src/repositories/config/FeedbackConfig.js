export const FeedbackConfig = {
  createMessage: '/collaboration/createMessage/dataflow/{:dataflowId}',
  importFile: '/createMessage/dataflow/{:dataflowId}/attachment',
  getAllMessages: '/collaboration/findMessages/dataflow/{:dataflowId}?page={:page}&providerId={:providerId}',
  getMessageAttachment: '/findMessages/dataflow/{:dataflowId}/getMessageAttachment',
  markMessagesAsRead: '/collaboration/updateMessageReadStatus/dataflow/{:dataflowId}'
};
