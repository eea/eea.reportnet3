const getTableName = state => {
  return state.datasets.datasetSchemas
    .find(dataset => dataset.datasetId === state.selectedDataset.value)
    .tables.find(table => table.tableSchemaId === state.selectedTable.value).tableSchemaName;
};
const getFieldName = state => {
  return state.datasets.datasetSchemas
    .find(dataset => dataset.datasetId === state.selectedDataset.value)
    .tables.find(table => table.tableSchemaId === state.selectedTable.value)
    .fields.find(field => field.fieldId === state.selectedField.value).name;
};
export const parseHelpItem = (type, state) => {
  if (type === 'field' && state.selectedField !== '') {
    return `dataset_${state.selectedDataset.value}."${getTableName(state)}"."${getFieldName(state)}"`;
  }
  if (type === 'table' && state.selectedTable !== '') {
    return `dataset_${state.selectedDataset.value}."${getTableName(state)}"`;
  }
  if (type === 'dataset' && state.selectedDataset !== '') {
    return `dataset_${state.selectedDataset.value}`;
  }
  return '';
};
