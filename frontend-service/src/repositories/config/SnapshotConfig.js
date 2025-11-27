export const SnapshotConfig = {
  createDesigner: '/snapshot/dataschema/{:datasetSchemaId}/dataset/{:datasetId}/create?description={:description}',
  createReporter: '/snapshot/dataset/{:datasetId}/create',
  deleteDesigner: '/snapshot/{:snapshotId}/dataschema/{:datasetSchemaId}/delete',
  deleteReporter: '/snapshot/v1/{:snapshotId}/dataset/{:datasetId}/delete',
  getAllDesigner: '/snapshot/dataschema/{:datasetSchemaId}/listSnapshots',
  getAllReporter: '/snapshot/dataset/{:datasetId}/listSnapshots',
  release:
    '/orchestrator/jobs/addRelease/dataflow/{:dataflowId}/dataProvider/{:dataProviderId}/release?restrictFromPublic={:restrictFromPublic}',
  silentRelease:
    '/orchestrator/jobs/addRelease/dataflow/{:dataflowId}/dataProvider/{:dataProviderId}/release?validate=true&restrictFromPublic={:restrictFromPublic}&silentRelease=true',
  restoreDesigner: '/snapshot/{:snapshotId}/dataschema/{:datasetSchemaId}/restore',
  restoreReporter: '/snapshot/{:snapshotId}/dataset/{:datasetId}/restore',
  updateReleaseDate:
    '/snapshot/v1/{:snapshotId}/dataset/{:datasetId}/updateReleaseDate?newReleaseDate={:newReleaseDate}'
};
