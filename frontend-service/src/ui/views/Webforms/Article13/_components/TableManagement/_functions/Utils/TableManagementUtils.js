import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { RecordUtils } from 'ui/views/_functions/Utils/RecordUtils';

const getFieldSchemaColumnIdByHeader = (tableSchemaColumns, header) => {
  const filteredSchemaColumn = tableSchemaColumns.filter(
    tableSchemaColumn => tableSchemaColumn.header.toUpperCase() === header.toUpperCase()
  );
  if (!isNil(filteredSchemaColumn) && !isEmpty(filteredSchemaColumn)) {
    return filteredSchemaColumn[0].field;
  } else {
    return '';
  }
};

const parseListOfSinglePams = (columns = [], records = []) => {
  const options = records
    .filter(record => record.IsGroup === 'Single')
    .map(singleRecord => `#${singleRecord.Id} - ${singleRecord.Title}`);

  return columns.map(column => {
    if (column.header === 'ListOfSinglePams') {
      column.type = 'MULTISELECT_CODELIST';
      column.codelistItems = options;
    }

    return column;
  });
};

const parseTableSchemaColumns = (schemaTables, records) => {
  const columns = [];
  schemaTables
    .filter(schemaTable => !isNil(schemaTable.tableSchemaName) && schemaTable.tableSchemaName.toUpperCase() === 'PAMS')
    .forEach(table => {
      if (!isNil(table.records)) {
        return table.records[0].fields.forEach(field => {
          columns.push({
            codelistItems: field['codelistItems'],
            description: field['description'],
            field: field['fieldId'],
            header: field['name'],
            pk: field['pk'],
            maxSize: field['maxSize'],
            pkHasMultipleValues: field['pkHasMultipleValues'],
            readOnly: field['readOnly'],
            recordId: field['recordId'],
            referencedField: field['referencedField'],
            table: table['tableSchemaName'],
            type: field['type'],
            validExtensions: field['validExtensions']
          });
        });
      }
    });

  return parseListOfSinglePams(columns, records);
};

const parsePamsRecords = records =>
  records.map(record => {
    const { recordId, recordSchemaId } = record;
    let data = {};

    record.elements.forEach(
      element =>
        (data = {
          ...data,
          [element.name]: element.value,
          recordId: recordId,
          recordSchemaId: recordSchemaId
        })
    );

    return data;
  });

const parsePamsRecordsWithParentData = (records, parentTablesWithData, schemaTables) => {
  const getFilteredData = () => {
    if (isNil(parentTablesWithData) || isNil(schemaTables)) {
      return '';
    }
    const filteredParentTablesWithData = parentTablesWithData.filter(
      parentTableWithData => parentTableWithData.tableSchemaName.toUpperCase() !== 'PAMS'
    );

    const filteredparentTablesNames = filteredParentTablesWithData.map(
      filteredparentTable => filteredparentTable.tableSchemaName
    );

    const filteredSchemaTables = schemaTables
      .filter(filteredSchemaTable => filteredparentTablesNames.includes(filteredSchemaTable.header))
      .map(table => {
        return {
          tableSchemaId: table.tableSchemaId,
          tableSchemaName: table.tableSchemaName,
          pamField: table.records[0].fields.filter(field => field.name.toUpperCase() === 'FK_PAMS')[0]
        };
      });
    return { filteredParentTablesWithData, filteredSchemaTables };
  };

  const { filteredParentTablesWithData, filteredSchemaTables } = getFilteredData();

  const getAdditionalInfo = record => {
    if (isNil(parentTablesWithData) || isNil(schemaTables)) {
      return '';
    }

    const getHasRecordByPaMId = table => {
      const filteredSchemaTablesPams = filteredSchemaTables.filter(
        filteredSchemaTable => filteredSchemaTable.tableSchemaId === table.tableSchemaId
      );
      if (!isNil(filteredSchemaTablesPams) && !isEmpty(filteredSchemaTablesPams)) {
        const fkPamId = filteredSchemaTablesPams[0].pamField.fieldSchema;
        const pamSchemaId = schemaTables
          .filter(table => table.header.toUpperCase() === 'PAMS')[0]
          .records[0].fields.filter(field => field.name.toUpperCase() === 'ID')[0];
        if (!isNil(pamSchemaId) && !isEmpty(pamSchemaId)) {
          const pamValue = RecordUtils.getCellValue({ rowData: record }, pamSchemaId.fieldId);
          let hasRecord = false;
          table.data.records.forEach(record => {
            if (!isEmpty(record.fields)) {
              record.fields.forEach(field => {
                if (field.fieldSchemaId === fkPamId && parseInt(field.value) === parseInt(pamValue)) {
                  hasRecord = true;
                }
              });
            }
          });
          return hasRecord;
        } else {
          return false;
        }
      } else {
        return false;
      }
    };

    const parentTablesIds = filteredParentTablesWithData.map(table => {
      return {
        tableSchemaId: table.tableSchemaId,
        tableSchemaName: table.tableSchemaName,
        hasRecord: getHasRecordByPaMId(table)
      };
    });
    return { tableSchemas: parentTablesIds };
  };

  return records.map(record => {
    const additionalInfo = getAdditionalInfo(record);
    return {
      ...record,
      dataRow: record.dataRow.map(element => {
        return {
          ...element,
          fieldData: {
            ...element.fieldData,
            tableSchemas: additionalInfo.tableSchemas
          }
        };
      })
    };
  });
};

export const TableManagementUtils = {
  getFieldSchemaColumnIdByHeader,
  parsePamsRecordsWithParentData,
  parsePamsRecords,
  parseTableSchemaColumns
};
