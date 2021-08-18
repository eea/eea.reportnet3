export const DataCollectionConfig = {
  create:
    '/datacollection/create?manualCheck={:isManualTechnicalAcceptance}&stopAndNotifySQLErrors={:stopAndNotifySQLErrors}&showPublicInfo={:showPublicInfo}',
  createReference: '/datacollection/create?stopAndNotifyPKError={:stopAndNotifyPKError}',
  update: '/datacollection/update/{:dataflowId}'
};
