export class DataSet {
  constructor(errors, dataSetId, dataSetSchemaId, dataSetSchemaName, totalErrors) {
    this.errors = errors;
    this.dataSetId = dataSetId;
    this.dataSetSchemaId = dataSetSchemaId;
    this.dataSetSchemaName = dataSetSchemaName;
    this.totalErrors = totalErrors;
  }
}
