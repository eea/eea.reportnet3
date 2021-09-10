export const DocumentConfig = {
  getAll: '/document/{:dataflowId}',
  getAllPublic: '/document/{:dataflowId}',
  delete: '/document/{:documentId}',
  download: '/document/{:documentId}',
  publicDownload: '/publicDocument/{:documentId}',
  update:
    '/document/update/{:documentId}/dataflow/{:dataflowId}?description={:description}&language={:language}&isPublic={:isPublic}',
  upload: '/document/upload/{:dataflowId}?description={:description}&language={:language}&isPublic={:isPublic}'
};
