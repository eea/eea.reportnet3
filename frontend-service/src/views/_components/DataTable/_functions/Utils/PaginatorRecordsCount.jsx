import { useContext } from 'react';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const PaginatorRecordsCount = ({ dataLength, filteredData, isFiltered }) => {
  const getRecordsDifferentFiltered = () => {
    if (isFiltered && dataLength === filteredData.length) {
      return '';
    }

    return `${messageFiltered}: ${filteredData.length} | `;
  };

  const getRecordsEqualsFiltered = () => {
    if (isFiltered && dataLength !== filteredData.length) {
      return '';
    }

    return ` (${messageFiltered.toLowerCase()})`;
  };

  const resourcesContext = useContext(ResourcesContext);

  const messageFiltered = resourcesContext.messages['filtered'];
  const messageRecords = resourcesContext.messages['records'];
  const messageTotalRecords = resourcesContext.messages['totalRecords'];
  const recordsTotal = `${messageTotalRecords} ${dataLength} ${messageRecords.toLowerCase()}`;

  return `${getRecordsDifferentFiltered()}${recordsTotal}${getRecordsEqualsFiltered()}`;
};
