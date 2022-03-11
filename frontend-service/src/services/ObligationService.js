import dayjs from 'dayjs';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import utc from 'dayjs/plugin/utc';

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
    dayjs.extend(utc);

    const getOpenedObligationsDTO = async () => {
      if (!isEmpty(filterData)) {
        const countryId = !isNil(filterData.countries) ? filterData.countries.value : '';
        const dateFrom =
          !isNil(filterData.expirationDate) && filterData.expirationDate[0]
            ? new Date(dayjs(filterData.expirationDate[0]).utc(true).valueOf()).getTime()
            : '';
        const dateTo =
          !isNil(filterData.expirationDate) && filterData.expirationDate[1]
            ? new Date(dayjs(filterData.expirationDate[1]).utc(true).endOf('day').valueOf()).getTime()
            : '';
        const issueId = !isNil(filterData.issues) ? filterData.issues.value : '';
        const organizationId = !isNil(filterData.organizations) ? filterData.organizations.value : '';

        return await ObligationRepository.getOpen(countryId, dateFrom, dateTo, issueId, organizationId);
      } else {
        return await ObligationRepository.getOpen();
      }
    };

    const openedObligationsDTO = await getOpenedObligationsDTO();
    const { totalRecords, filteredRecords, obligations } = openedObligationsDTO?.data;
    const parseobligationList = ObligationUtils.parseObligationList(obligations);

    return { filteredRecords, obligations: parseobligationList, totalRecords };
  }
};
