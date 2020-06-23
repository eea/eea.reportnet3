const parseDataflow = dataflow => ({
  dueDate: dataflow.expirationDate,
  id: dataflow.id,
  status: dataflow.status,
  subtitle: dataflow.description,
  title: dataflow.name
});

const parseDataflowsList = dataflowList => dataflowList.map(dataflow => parseDataflow(dataflow));

export const CloneSchemasUtils = { parseDataflowsList };
