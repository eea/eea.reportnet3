export const ObligationConfig = {
  get: '/obligation/{:obligationId}',
  getOpen:
    '/obligation/findOpened?clientId={:organizationId}&deadlineDateFrom={:dateFrom}&deadlineDateTo={:dateTo}&issueId={:issueId}&spatialId={:countryId}',
  getCountries: '/obligation_country/',
  getIssues: '/obligation_issue/',
  getOrganizations: '/obligation_client/'
};
