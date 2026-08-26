export const DataCollectionConfig = {
  create:
    '/datacollection/create?manualCheck={:isManualTechnicalAcceptance}&stopAndNotifySQLErrors={:stopAndNotifySQLErrors}&showPublicInfo={:showPublicInfo}&disableRulesEventChoice={:disableRulesEventChoice}',
  createReference: '/datacollection/create?stopAndNotifyPKError={:stopAndNotifyPKError}',
  update: '/datacollection/update/{:dataflowId}'
};
