export class DatasetTable {
  constructor({
    hasErrors,
    hasPKReferenced,
    records,
    recordSchemaId,
    recordsWithErrorsPercentage,
    recordsWithoutErrorsPercentage,
    recordsWithWarningsPercentage,
    tableSchemaDescription,
    tableSchemaFixedNumber,
    tableSchemaId,
    tableSchemaName,
    tableSchemaNotEmpty,
    tableSchemaReadOnly,
    tableSchemaToPrefill,
    totalRecords
  } = {}) {
    this.hasErrors = hasErrors;
    this.hasPKReferenced = hasPKReferenced;
    this.records = records;
    this.recordSchemaId = recordSchemaId;
    this.recordsWithErrorsPercentage = recordsWithErrorsPercentage;
    this.recordsWithoutErrorsPercentage = recordsWithoutErrorsPercentage;
    this.recordsWithWarningsPercentage = recordsWithWarningsPercentage;
    this.tableSchemaDescription = tableSchemaDescription;
    this.tableSchemaFixedNumber = tableSchemaFixedNumber;
    this.tableSchemaId = tableSchemaId;
    this.tableSchemaName = tableSchemaName;
    this.tableSchemaNotEmpty = tableSchemaNotEmpty;
    this.tableSchemaReadOnly = tableSchemaReadOnly;
    this.tableSchemaToPrefill = tableSchemaToPrefill;
    this.totalRecords = totalRecords;
  }
}
