export const FeedbackConfig = {
  createMessage: '/collaboration/createMessage/dataflow/{:dataflowId}',
  deleteMessage: '/collaboration/deleteMessage/dataflow/{:dataflowId}?providerId={:providerId}&messageId={:messageId}',
  importFile: '/collaboration/createMessage/dataflow/{:dataflowId}/attachment?providerId={:providerId}',
  getAllMessages: '/collaboration/findMessages/dataflow/{:dataflowId}?page={:page}&providerId={:providerId}',
  getMessageAttachment:
    '/collaboration/findMessages/dataflow/{:dataflowId}/getMessageAttachment?providerId={:providerId}&messageId={:messageId}',
  markMessagesAsRead: '/collaboration/updateMessageReadStatus/dataflow/{:dataflowId}'
};
