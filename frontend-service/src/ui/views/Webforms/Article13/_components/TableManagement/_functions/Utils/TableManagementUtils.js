import { isEmpty } from 'lodash';
import isNil from 'lodash/isNil';

import { RecordUtils } from 'ui/views/_functions/Utils/RecordUtils';

const getFieldSchemaColumnIdByHeader = (tableSchemaColumns, header) => {
  const filteredSchemaColumn = tableSchemaColumns.filter(tableSchemaColumn => tableSchemaColumn.header === header);
  if (!isNil(filteredSchemaColumn) && !isEmpty(filteredSchemaColumn)) {
    return filteredSchemaColumn[0].field;
  } else {
    return '';
  }
};

const parseTableSchemaColumns = schemaTables => {
  console.log(schemaTables);
  const columns = [];
  schemaTables
    .filter(schemaTable => schemaTable.tableSchemaName === 'PaMs')
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
  return columns;
};

const parsePamsRecords = records =>
  records.map(record => {
    const { recordId, recordSchemaId } = record;
    let data = {};
    debugger;
    record.elements.forEach(
      element =>
        (data = {
          ...data,
          [element.name]: element.value,
          recordId: recordId,
          recordSchemaId: recordSchemaId
        })
    );
  });

const parsePamsRecordsWithParentData = (records, parentTablesWithData, schemaTables) => {
  const getFilteredData = () => {
    if (isNil(parentTablesWithData) || isNil(schemaTables)) {
      return '';
    }
    const filteredParentTablesWithData = parentTablesWithData.filter(
      parentTableWithData => parentTableWithData.tableSchemaName !== 'PaMs'
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
          pamField: table.records[0].fields.filter(field => field.name === 'Fk_PaMs')[0]
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
      console.log({ filteredSchemaTablesPams, schemaTables });
      if (!isNil(filteredSchemaTablesPams) && !isEmpty(filteredSchemaTablesPams)) {
        const fkPamId = filteredSchemaTablesPams[0].pamField.fieldSchema;
        const pamSchemaId = schemaTables
          .filter(table => table.header === 'PaMs')[0]
          .records[0].fields.filter(field => field.name === 'Id')[0].fieldId;
        const pamValue = RecordUtils.getCellValue({ rowData: record }, pamSchemaId);
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
    const { recordId, recordSchemaId } = record;
    const additionalInfo = getAdditionalInfo(record);
    // let data = {};

    console.log({ record });
    // const fields = [];

    // data.dataRow = fields;
    // data.recordId = recordId;
    // data.recordSchemaId = recordSchemaId;

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
