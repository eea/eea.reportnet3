export const DocumentConfig = {
  delete: '/document/{:documentId}',
  download: '/document/{:documentId}',
  update:
    '/document/update/{:documentId}/dataflow/{:dataflowId}?description={:description}&language={:language}&isPublic={:isPublic}',
  upload: '/document/upload/{:dataflowId}?description={:description}&language={:language}&isPublic={:isPublic}'
};
