export class DataSetTable {
  constructor(
    hasErrors,
    tableSchemaId,
    tableSchemaName,
    recordsWithoutErrorsPercentage,
    recordsWithErrorsPercentage,
    recordsWithWarningsPercentage,
    records
  ) {
    this.hasErrors = hasErrors;
    this.records = records;
    this.recordsWithoutErrorsPercentage = recordsWithoutErrorsPercentage;
    this.recordsWithErrorsPercentage = recordsWithErrorsPercentage;
    this.recordsWithWarningsPercentage = recordsWithWarningsPercentage;
    this.tableSchemaId = tableSchemaId;
    this.tableSchemaName = tableSchemaName;
  }
}
