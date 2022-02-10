const getPaginatorRecordsCount = ({ dataLength, filteredData, isFiltered, resourcesContext }) => {
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

  const messageFiltered = resourcesContext.messages['filtered'];
  const messageRecords = resourcesContext.messages['records'];
  const messageTotalRecords = resourcesContext.messages['totalRecords'];
  const recordsTotal = `${messageTotalRecords} ${dataLength} ${messageRecords.toLowerCase()}`;

  return `${getRecordsDifferentFiltered()}${recordsTotal}${getRecordsEqualsFiltered()}`;
};

export const PaginatorRecordsCount = { getPaginatorRecordsCount };
