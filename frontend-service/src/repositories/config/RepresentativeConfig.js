export const RepresentativeConfig = {
  add: '/representative/{:dataflowId}',
  addLeadReporter: '/representative/{:representativeId}/leadReporter/dataflow/{:dataflowId}',
  allDataProviders: '/representative/dataProvider/{:dataProviderGroupId}',
  allRepresentatives: '/representative/dataflow/{:dataflowId}',
  deleteById: '/representative/{:representativeId}/dataflow/{:dataflowId}',
  deleteLeadReporter: '/representative/leadReporter/{:leadReporterId}/dataflow/{:dataflowId}',
  exportRepresentatives: '/representative/export/{:dataflowId}',
  exportRepresentativesTemplate: '/representative/exportTemplateReportersFile/{:dataProviderGroupId}',
  getFmeUsers: '/representative/fmeUsers',
  getGroupProviders: '/representative/dataProvider/countryGroups',
  getGroupCompanies: '/representative/dataProvider/companyGroups',
  importLeadReporters: '/representative/import/{:dataflowId}/group/{:dataProviderGroupId}',
  updateDataProviderId: '/representative/update',
  updateLeadReporter: '/representative/leadReporter/update/dataflow/{:dataflowId}'
};
