export const DocumentConfig = {
  getAll: '/document/{:dataflowId}',
  delete: '/document/{:documentId}/dataflow/{:dataflowId}',
  download: '/document/{:documentId}/dataflow/{:dataflowId}',
  downloadPublic: '/document/{:documentId}/dataflow/{:dataflowId}',
  update:
    '/document/update/{:documentId}/dataflow/{:dataflowId}?description={:description}&language={:language}&isPublic={:isPublic}',
  upload: '/document/upload/{:dataflowId}?description={:description}&language={:language}&isPublic={:isPublic}'
};
