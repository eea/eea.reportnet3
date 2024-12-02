import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import remove from 'lodash/remove';

import { RecordUtils } from 'views/_functions/Utils/RecordUtils';

import { TextUtils } from 'repositories/_utils/TextUtils';

const getFieldSchemaColumnIdByHeader = (tableSchemaColumns, header) => {
  const filteredSchemaColumn = tableSchemaColumns.filter(tableSchemaColumn =>
    TextUtils.areEquals(tableSchemaColumn.header, header)
  );
  if (!isNil(filteredSchemaColumn) && !isEmpty(filteredSchemaColumn)) {
    return filteredSchemaColumn[0].field;
  } else {
    return '';
  }
};

const getSingleRecordOption = singleRecord => {
  if (singleRecord[Object.keys(singleRecord).find(key => TextUtils.areEquals(key, 'TITLE'))] === '') {
    return `#${singleRecord[Object.keys(singleRecord).find(key => TextUtils.areEquals(key, 'ID'))]}`;
  }

  return `#${singleRecord[Object.keys(singleRecord).find(key => TextUtils.areEquals(key, 'ID'))]} - ${
    singleRecord[Object.keys(singleRecord).find(key => TextUtils.areEquals(key, 'TITLE'))]
  }`;
};

const parseListOfSingleEntities = (columns = [], records = []) => {
  const options = records
    .filter(record => record.IsGroup === 'Single')
    .map(singleRecord => {
      if (
        Object.keys(singleRecord)
          .map(key => key.toUpperCase())
          .includes('ID', 'TITLE')
      ) {
        return getSingleRecordOption(singleRecord);
      }

      return null;
    })
    .sort((a, b) => a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' }));

  return columns.map(column => {
    if (column.header === 'ListOfSingleEntities') {
      column.type = 'MULTISELECT_CODELIST';
      column.codelistItems = remove(options, undefined || null);
    }

    return column;
  });
};

const parseTableSchemaColumns = (schemaTables, records, rootTableName) => {
  const columns = [];
  schemaTables
    .filter(schemaTable => TextUtils.areEquals(schemaTable.tableSchemaName, rootTableName))
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

  return parseListOfSingleEntities(columns, records);
};

const parseEntitiesRecordsWithParentData = (
  records,
  parentTablesWithData,
  schemaTables,
  rootPkFieldId,
  rootTableName
) => {
  const getFilteredData = () => {
    if (isNil(parentTablesWithData) || isNil(schemaTables)) {
      return '';
    }

    const filteredParentTablesWithData = parentTablesWithData.filter(
      parentTableWithData => !TextUtils.areEquals(parentTableWithData.tableSchemaName, rootTableName)
    );

    const filteredparentTablesNames = filteredParentTablesWithData.map(
      filteredparentTable => filteredparentTable.tableSchemaName
    );

    const filteredSchemaTables = schemaTables
      .filter(
        filteredSchemaTable =>
          filteredparentTablesNames.includes(filteredSchemaTable.tableSchemaName) ||
          filteredparentTablesNames.includes(filteredSchemaTable.header)
      )
      .map(table => {
        const filteredFkFieldId = table.records[0].fields.filter(field => {
          if (field?.referencedField) {
            return Object.values(field?.referencedField).includes(rootPkFieldId);
          }
          return '';
        })[0];

        return {
          tableSchemaId: table.tableSchemaId,
          tableSchemaName: table.tableSchemaName,
          fkRootField: filteredFkFieldId
        };
      });
    return { filteredParentTablesWithData, filteredSchemaTables };
  };

  const { filteredParentTablesWithData, filteredSchemaTables } = getFilteredData();

  const getAdditionalInfo = record => {
    if (isNil(parentTablesWithData) || isNil(schemaTables)) {
      return '';
    }

    const getHasRecordByEntityId = table => {
      const filteredSchemaTablesEntities = filteredSchemaTables.filter(
        filteredSchemaTable => filteredSchemaTable.tableSchemaId === table.tableSchemaId
      );
      if (!isNil(filteredSchemaTablesEntities) && !isEmpty(filteredSchemaTablesEntities)) {
        const fkEntityId =
          filteredSchemaTablesEntities[0].fkRootField.fieldSchema ||
          filteredSchemaTablesEntities[0].fkRootField.fieldId;
        const rootSchemaId = schemaTables
          .filter(table => TextUtils.areEquals(table.tableSchemaName, rootTableName))[0]
          .records[0].fields.filter(field => TextUtils.areEquals(field.pk, true))[0];

        if (!isNil(rootSchemaId) && !isEmpty(rootSchemaId)) {
          const entityValue = RecordUtils.getCellValue({ rowData: record }, rootSchemaId.fieldId);
          let hasRecord = false;
          table.data.records.forEach(record => {
            if (!isEmpty(record.fields)) {
              record.fields.forEach(field => {
                if (field.fieldSchemaId === fkEntityId && parseInt(field.value) === parseInt(entityValue)) {
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
        hasRecord: getHasRecordByEntityId(table)
      };
    });
    return { tableSchemas: parentTablesIds };
  };

  return records.map(record => {
    const additionalInfo = getAdditionalInfo(record);
    return {
      ...record,
      dataRow: record.dataRow.map(element => {
        return { ...element, fieldData: { ...element.fieldData, tableSchemas: additionalInfo.tableSchemas } };
      })
    };
  });
};

export const TableManagementUtils = {
  getFieldSchemaColumnIdByHeader,
  parseEntitiesRecordsWithParentData,
  parseTableSchemaColumns
};
