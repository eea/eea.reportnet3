export class DataSetStatistics {
  constructor(idDataset, idDatasetSchema, nameDataSetSchema, errors, totalErrors) {
    this.idDataset = idDataset;
    this.idDatasetSchema = idDatasetSchema;
    this.nameDataSetSchema = nameDataSetSchema;
    this.totalErrors = totalErrors;
    this.errors = errors;
  }
}
