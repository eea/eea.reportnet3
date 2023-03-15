import { useContext } from 'react';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const PaginatorRecordsCount = ({
  dataLength,
  filteredDataLength,
  remainingJobsLength,
  isFiltered,
  nameRecords = 'records'
}) => {
  const resourcesContext = useContext(ResourcesContext);

  const getRemainingJobs = () => {
    if (!remainingJobsLength) {
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

  return `${getRemainingJobs()}${getRecordsDifferentFiltered()}${recordsTotal}${getRecordsEqualsFiltered()}`;
};
