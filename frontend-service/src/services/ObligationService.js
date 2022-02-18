import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { ObligationUtils } from 'services/_utils/ObligationUtils';

import { ObligationRepository } from 'repositories/ObligationRepository';

export const ObligationService = {
  getCountries: async () => {
    const countriesDTO = await ObligationRepository.getCountries();
    return ObligationUtils.parseCountryList(countriesDTO.data);
  },

  getIssues: async () => {
    const issuesDTO = await ObligationRepository.getIssues();
    return ObligationUtils.parseIssueList(issuesDTO.data);
  },

  getOrganizations: async () => {
    const clientsDTO = await ObligationRepository.getOrganizations();
    return ObligationUtils.parseOrganizationList(clientsDTO.data);
  },

  get: async obligationId => {
    const obligationByIdDTO = await ObligationRepository.get(obligationId);
    return ObligationUtils.parseObligation(obligationByIdDTO.data);
  },

  getOpen: async filterData => {
    if (!isEmpty(filterData)) {
      const countryId = !isNil(filterData.countries) ? filterData.countries.value : '';
      const dateFrom =
        !isNil(filterData.expirationDate) && filterData.expirationDate[0] ? filterData.expirationDate[0] : '';
      const dateTo =
        !isNil(filterData.expirationDate) && filterData.expirationDate[1] ? filterData.expirationDate[1] : '';
      const issueId = !isNil(filterData.issues) ? filterData.issues.value : '';
      const organizationId = !isNil(filterData.organizations) ? filterData.organizations.value : '';
      const openedObligationsDTO = await ObligationRepository.getOpen(
        countryId,
        dateFrom,
        dateTo,
        issueId,
        organizationId
      );
      const { totalRecords, filteredRecords, obligations } = openedObligationsDTO?.data;
      const parseobligationList = ObligationUtils.parseObligationList(obligations);

      return { filteredRecords, obligations: parseobligationList, totalRecords };
    } else {
      const openedObligationsDTO = await ObligationRepository.getOpen();
      const { totalRecords, filteredRecords, obligations } = openedObligationsDTO?.data;
      const parseobligationList = ObligationUtils.parseObligationList(obligations);

      return { filteredRecords, obligations: parseobligationList, totalRecords };
    }
  }
};
