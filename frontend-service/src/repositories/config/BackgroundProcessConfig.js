export const BackgroundProcessConfig = {
  getValidationsStatuses:
    '/process/?asc={:sortOrder}&pageNum={:pageNum}&pageSize={:numberRows}&header={:sortField}&user={:user}&dataflowId={:dataflowId}&status={:status}',
  update: '/process/{:processId}/priority/{:priority}'
};
