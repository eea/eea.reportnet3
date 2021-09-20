export const DocumentConfig = {
  getAll: '/document/dataflow/{:dataflowId}',
  delete: '/document/{:documentId}/dataflow/{:dataflowId}',
  download: '/document/{:documentId}/dataflow/{:dataflowId}',
  publicDownload: '/document/public/{:documentId}',
  update:
    '/document/update/{:documentId}/dataflow/{:dataflowId}?description={:description}&language={:language}&isPublic={:isPublic}',
  upload: '/document/upload/{:dataflowId}?description={:description}&language={:language}&isPublic={:isPublic}'
};
