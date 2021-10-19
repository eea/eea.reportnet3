export const SnapshotConfig = {
  createDesigner: '/snapshot/dataschema/{:datasetSchemaId}/dataset/{:datasetId}/create?description={:description}',
  createReporter: '/snapshot/dataset/{:datasetId}/create',
  deleteDesigner: '/snapshot/{:snapshotId}/dataschema/{:datasetSchemaId}/delete',
  deleteReporter: '/snapshot/{:snapshotId}/dataset/{:datasetId}/delete',
  getAllDesigner: '/snapshot/dataschema/{:datasetSchemaId}/listSnapshots',
  getAllReporter: '/snapshot/dataset/{:datasetId}/listSnapshots',
  release:
    '/snapshot/dataflow/{:dataflowId}/dataProvider/{:dataProviderId}/release?restrictFromPublic={:restrictFromPublic}',
  restoreDesigner: '/snapshot/{:snapshotId}/dataschema/{:datasetSchemaId}/restore',
  restoreReporter: '/snapshot/{:snapshotId}/dataset/{:datasetId}/restore',
  update:
    '/snapshot/update/dataflow/{:dataflowId}/dataProvider/{:dataProviderId}/restrictFromPublic={:restrictFromPublic}' // TODO ADD REAL ENDPOINT
};
