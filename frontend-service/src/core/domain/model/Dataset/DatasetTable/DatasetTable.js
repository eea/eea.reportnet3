export class DatasetTable {
  constructor({
    hasErrors,
    hasPKReferenced,
    recordSchemaId,
    records,
    recordsWithErrorsPercentage,
    recordsWithWarningsPercentage,
    recordsWithoutErrorsPercentage,
    tableSchemaDescription,
    tableSchemaId,
    tableSchemaName,
    tableSchemaReadOnly,
    tableSchemaToPrefill,
    totalRecords
  } = {}) {
    this.hasErrors = hasErrors;
    this.hasPKReferenced = hasPKReferenced;
    this.records = records;
    this.recordSchemaId = recordSchemaId;
    this.recordsWithoutErrorsPercentage = recordsWithoutErrorsPercentage;
    this.recordsWithErrorsPercentage = recordsWithErrorsPercentage;
    this.recordsWithWarningsPercentage = recordsWithWarningsPercentage;
    this.tableSchemaDescription = tableSchemaDescription;
    this.tableSchemaToPrefill = tableSchemaToPrefill;
    this.tableSchemaId = tableSchemaId;
    this.tableSchemaName = tableSchemaName;
    this.tableSchemaReadOnly = tableSchemaReadOnly;
    this.totalRecords = totalRecords;
  }
}
