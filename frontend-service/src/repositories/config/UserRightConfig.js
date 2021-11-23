export const UserRightConfig = {
  getReporters: '/contributor/reporter/dataflow/{:dataflowId}/provider/{:dataProviderId}',
  getRequesters: '/contributor/requester/dataflow/{:dataflowId}',
  deleteReporter: '/contributor/reporter/dataflow/{:dataflowId}/provider/{:dataProviderId}',
  deleteRequester: '/contributor/requester/dataflow/{:dataflowId}',
  updateReporter: '/contributor/reporter/dataflow/{:dataflowId}/provider/{:dataProviderId}',
  updateRequester: '/contributor/requester/dataflow/{:dataflowId}',
  validateReporters: '/contributor/validateReporters/dataflow/{:dataflowId}/provider/{:dataProviderId}'
};
