export const GetPublicDataflowsByCountryCode = ({ dataflowRepository }) => async countryCode =>
  dataflowRepository.getPublicDataflowsByCountryCode(countryCode);
