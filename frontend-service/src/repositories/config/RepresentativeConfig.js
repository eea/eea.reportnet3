export const RepresentativeConfig = {
  createDataProvider: '/representative/{:dataflowId}',
  createLeadReporter: '/representative/{:representativeId}/leadReporter/dataflow/{:dataflowId}',
  deleteLeadReporter: '/representative/leadReporter/{:leadReporterId}/dataflow/{:dataflowId}',
  deleteRepresentative: '/representative/{:representativeId}/dataflow/{:dataflowId}',
  exportFile: '/representative/export/{:dataflowId}',
  exportTemplateFile: '/representative/exportTemplateReportersFile/{:dataProviderGroupId}',
  getDataProviders: '/representative/dataProvider/{:dataProviderGroupId}',
  getFmeUsers: '/representative/fmeUsers',
  getGroupCompanies: '/representative/dataProvider/companyGroups',
  getGroupCountries: '/representative/dataProvider/countryGroups',
  getGroupOrganizations: '/representative/dataProvider/organizationGroups',
  getRepresentatives: '/representative/dataflow/{:dataflowId}',
  getSelectedDataProviderGroup: '/representative/dataProviderGroup/dataflow/{:dataflowId}',
  importFile: '/representative/import/{:dataflowId}/group/{:dataProviderGroupId}',
  updateDataProviderId: '/representative/update',
  updateLeadReporter: '/representative/leadReporter/update/dataflow/{:dataflowId}'
};
