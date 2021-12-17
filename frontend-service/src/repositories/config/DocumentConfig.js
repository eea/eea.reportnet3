export const DocumentConfig = {
  getAll: '/document/v1/dataflow/{:dataflowId}',
  delete: '/document/v1/{:documentId}/dataflow/{:dataflowId}',
  download: '/document/v1/{:documentId}/dataflow/{:dataflowId}',
  publicDownload: '/document/public/{:documentId}',
  update:
    '/document/v1/update/{:documentId}/dataflow/{:dataflowId}?description={:description}&language={:language}&isPublic={:isPublic}',
  upload: '/document/v1/upload/{:dataflowId}?description={:description}&language={:language}&isPublic={:isPublic}'
};
