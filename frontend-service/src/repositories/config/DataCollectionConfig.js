export const DataCollectionConfig = {
  createDataCollection:
    '/datacollection/create?manualCheck={:isManualTechnicalAcceptance}&stopAndNotifySQLErrors={:stopAndNotifySQLErrors}&showPublicInfo={:showPublicInfo}',
  createReference: '/datacollection/create?stopAndNotifyPKError={:stopAndNotifyPKError}',
  updateDataCollectionNewRepresentatives: '/datacollection/update/{:dataflowId}'
};
