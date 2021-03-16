export const GetPublicDataflowsByCountryCode = ({ dataflowRepository }) => async (
  countryCode,
  sortOrder,
  pageNum,
  numberRows,
  sortField
) => dataflowRepository.getPublicDataflowsByCountryCode(countryCode, sortOrder, pageNum, numberRows, sortField);
