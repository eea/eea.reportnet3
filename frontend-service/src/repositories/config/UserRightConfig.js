export const UserRightConfig = {
  getNationalCoordinators: '/user/nationalCoordinator',
  getReporters: '/contributor/reporter/dataflow/{:dataflowId}/provider/{:dataProviderId}',
  getRequesters: '/contributor/requester/dataflow/{:dataflowId}',
  deleteNationalCoordinators: '/user/nationalCoordinator',
  deleteReporter: '/contributor/reporter/dataflow/{:dataflowId}/provider/{:dataProviderId}',
  deleteRequester: '/contributor/requester/dataflow/{:dataflowId}',
  updateNationalCoordinators: '/user/nationalCoordinator',
  updateReporter: '/contributor/reporter/dataflow/{:dataflowId}/provider/{:dataProviderId}',
  updateRequester: '/contributor/requester/dataflow/{:dataflowId}',
  validateReporters: '/contributor/validateReporters/dataflow/{:dataflowId}/provider/{:dataProviderId}'
};
