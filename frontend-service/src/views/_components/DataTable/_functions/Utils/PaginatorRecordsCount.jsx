import { useContext } from 'react';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const PaginatorRecordsCount = ({ dataLength, filteredDataLength, isFiltered }) => {
  const resourcesContext = useContext(ResourcesContext);

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
    'records'
  ].toLowerCase()}`;

  return `${getRecordsDifferentFiltered()}${recordsTotal}${getRecordsEqualsFiltered()}`;
};
