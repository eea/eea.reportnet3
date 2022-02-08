const getPaginatorRecordsCount = ({
  data,
  filteredData,
  isFiltered,
  messageFiltered,
  messageRecords,
  messageTotalRecords
}) => {
  let recordsCount = '';

  if (isFiltered && data.length !== filteredData.length) {
    recordsCount += `${messageFiltered}: ${filteredData.length} | `;
  }

  recordsCount += `${messageTotalRecords} ${data.length} ${messageRecords.toLowerCase()}`;

  if (isFiltered && data.length === filteredData.length) {
    recordsCount += ` (${messageFiltered.toLowerCase()})`;
  }

  return recordsCount;
};

export const PaginatorRecordsCount = { getPaginatorRecordsCount };
