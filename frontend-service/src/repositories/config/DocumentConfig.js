export const DocumentConfig = {
  deleteDocument: '/document/{:documentId}',
  editDocument:
    '/document/update/{:documentId}/dataflow/{:dataflowId}?description={:description}&language={:language}&isPublic={:isPublic}',
  downloadDocumentById: '/document/{:documentId}',
  uploadDocument: '/document/upload/{:dataflowId}?description={:description}&language={:language}&isPublic={:isPublic}'
};
