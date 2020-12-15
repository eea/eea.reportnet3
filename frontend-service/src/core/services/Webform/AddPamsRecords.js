export const AddPamsRecords = ({ webformRepository }) => async (datasetId, tables, pamId, type) => {
  return webformRepository.addPamsRecords(datasetId, tables, pamId, type);
};
