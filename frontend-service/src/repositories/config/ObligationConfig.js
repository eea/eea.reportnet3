export const ObligationConfig = {
  countries: '/obligation_country/',
  issues: '/obligation_issue/',
  obligationById: '/obligation/{:obligationId}',
  organizations: '/obligation_client/',
  openedObligations:
    '/obligation/findOpened?clientId={:organizationId}&deadlineDateFrom={:dateFrom}&deadlineDateTo={:dateTo}&issueId={:issueId}&spatialId={:countryId}'
};
