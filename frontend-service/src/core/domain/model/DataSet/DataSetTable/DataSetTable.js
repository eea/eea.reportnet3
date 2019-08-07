export class DataSetTable {
  constructor(
    hasErrors,
    tableSchemaId,
    tableSchemaName,
    recordsWithoutErrorsPercentage,
    recordsWithErrorsPercentage,
    recordsWithWarningsPercentage
  ) {
    this.hasErrors = hasErrors;
    this.recordsWithoutErrorsPercentage = recordsWithoutErrorsPercentage;
    this.recordsWithErrorsPercentage = recordsWithErrorsPercentage;
    this.recordsWithWarningsPercentage = recordsWithWarningsPercentage;
    this.tableSchemaId = tableSchemaId;
    this.tableSchemaName = tableSchemaName;
  }
}
