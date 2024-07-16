import { useContext } from 'react';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const PaginatorRecordsCount = ({
  dataLength,
  filteredDataLength,
  filteredJobsLength,
  remainingJobsLength,
  isFiltered,
  nameRecords = 'records'
}) => {
  const resourcesContext = useContext(ResourcesContext);

  const getFilteredJobs = () => {
    if (!filteredJobsLength) {
      return '';
    }

    return `${resourcesContext.messages['filteredJobs']}: ${filteredJobsLength} | `;
  };

  const getRemainingJobs = () => {
    if (!remainingJobsLength || filteredJobsLength) {
      return '';
    }

    return `${resourcesContext.messages['remainingJobs']}: ${remainingJobsLength} | `;
  };

  const getRecordsDifferentFiltered = () => {
    if (!isFiltered || dataLength === filteredDataLength) {
      return '';
    }

    return `${resourcesContext.messages['filtered']}: ${filteredDataLength} | `;
  };

  const getRecordsEqualsFiltered = () => {
    if (!isFiltered || dataLength !== filteredDataLength) {
      return '';
    }

    return ` (${resourcesContext.messages['filtered'].toLowerCase()})`;
  };

  const recordsTotal = `${resourcesContext.messages['totalRecords']} ${dataLength} ${resourcesContext.messages[
    nameRecords
  ].toLowerCase()}`;

  return `${getFilteredJobs()}${getRemainingJobs()}${getRecordsDifferentFiltered()}${recordsTotal}${getRecordsEqualsFiltered()}`;
};
