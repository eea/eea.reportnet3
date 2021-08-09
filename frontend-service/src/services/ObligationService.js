import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { ObligationsUtils } from '_utils/ObligationUtils.js';

import { ObligationRepository } from 'repositories/ObligationRepository';

export const ObligationService = {
  getCountries: async () => {
    const countriesDTO = await ObligationRepository.getCountries();
    return ObligationsUtils.parseCountryList(countriesDTO.data);
  },

  getIssues: async () => {
    const issuesDTO = await ObligationRepository.getIssues();
    return ObligationsUtils.parseIssueList(issuesDTO.data);
  },

  getOrganizations: async () => {
    const clientsDTO = await ObligationRepository.getOrganizations();
    return ObligationsUtils.parseOrganizationList(clientsDTO.data);
  },

  get: async obligationId => {
    const obligationByIdDTO = await ObligationRepository.get(obligationId);
    return ObligationsUtils.parseObligation(obligationByIdDTO.data);
  },

  getOpen: async filterData => {
    if (!isEmpty(filterData)) {
      const countryId = !isNil(filterData.countries) ? filterData.countries.value : '';
      const dateFrom = filterData.expirationDate[0] ? filterData.expirationDate[0].getTime() : '';
      const dateTo = filterData.expirationDate[1] ? filterData.expirationDate[1].getTime() : '';
      const issueId = !isNil(filterData.issues) ? filterData.issues.value : '';
      const organizationId = !isNil(filterData.organizations) ? filterData.organizations.value : '';
      const openedObligationsDTO = await ObligationRepository.getOpen(
        countryId,
        dateFrom,
        dateTo,
        issueId,
        organizationId
      );
      return ObligationsUtils.parseObligationList(openedObligationsDTO.data);
    } else {
      const openedObligationsDTO = await ObligationRepository.getOpen();
      return ObligationsUtils.parseObligationList(openedObligationsDTO.data);
    }
  }
};
