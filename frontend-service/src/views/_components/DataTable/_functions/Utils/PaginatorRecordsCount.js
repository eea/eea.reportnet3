const getPaginatorRecordsCount = ({
  dataLength,
  filteredDataLength,
  isFiltered,
  messageFiltered,
  messageRecords,
  messageTotalRecords
}) => {
  let recordsCount = '';

  if (isFiltered && dataLength !== filteredDataLength) {
    recordsCount += `${messageFiltered}: ${filteredDataLength} | `;
  }

  recordsCount += `${messageTotalRecords} ${dataLength} ${messageRecords.toLowerCase()}`;

  if (isFiltered && dataLength === filteredDataLength) {
    recordsCount += ` (${messageFiltered.toLowerCase()})`;
  }
  return recordsCount;
};

export const PaginatorRecordsCount = { getPaginatorRecordsCount };
