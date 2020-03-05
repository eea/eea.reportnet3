import React, { useState, useEffect } from 'react';

import { isEmpty, isUndefined, cloneDeep } from 'lodash';

import styles from '../../DataViewer.module.css';

import { config } from 'conf';

import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { IconTooltip } from 'ui/views/_components/IconTooltip';
import { DataViewerUtils } from '../Utils/DataViewerUtils';
import { RecordUtils } from 'ui/views/_functions/Utils';

export const useLoadColsSchemasAndColumnOptions = tableSchemaColumns => {
  const [columnOptions, setColumnOptions] = useState([{}]);
  const [colsSchema, setColsSchema] = useState(tableSchemaColumns);

  useEffect(() => {
    let colOptions = [];
    let dropdownFilter = [];

    for (let colSchema of colsSchema) {
      colOptions.push({ label: colSchema.header, value: colSchema });
      dropdownFilter.push({ label: colSchema.header, key: colSchema.field });
    }

    setColumnOptions(colOptions);

    const inmTableSchemaColumns = [...tableSchemaColumns];

    if (!isEmpty(inmTableSchemaColumns)) {
      inmTableSchemaColumns.push({ table: inmTableSchemaColumns[0].table, field: 'id', header: '' });
      inmTableSchemaColumns.push({ table: inmTableSchemaColumns[0].table, field: 'datasetPartitionId', header: '' });
    }

    setColsSchema(inmTableSchemaColumns);
  }, []);
  return {
    colsSchema,
    columnOptions
  };
};

export const useContextMenu = (resources, records, setEditDialogVisible, setConfirmDeleteVisible) => {
  const [menu, setMenu] = useState();

  useEffect(() => {
    setMenu([
      {
        label: resources.messages['edit'],
        icon: config.icons['edit'],
        command: () => {
          setEditDialogVisible(true);
        }
      },
      {
        label: resources.messages['delete'],
        icon: config.icons['trash'],
        command: () => setConfirmDeleteVisible(true)
      }
    ]);
  }, [records.selectedRecord]);
  return { menu };
};

export const useSetColumns = (
  actionTemplate,
  cellDataEditor,
  codelistInfo,
  colsSchema,
  columnOptions,
  hasWritePermissions,
  initialCellValue,
  isDataCollection,
  isWebFormMMR,
  records,
  resources,
  setCodelistInfo,
  setIsCodelistInfoVisible,
  validationsTemplate
) => {
  const [columns, setColumns] = useState([]);
  const [originalColumns, setOriginalColumns] = useState([]);

  useEffect(() => {
    const maxWidths = [];
    const providerCodeTemplate = rowData => {
      return (
        <div style={{ display: 'flex', alignItems: 'center' }}>
          {!isUndefined(rowData) ? rowData.providerCode : null}
        </div>
      );
    };
    // if (!isEditing) {
    //Calculate the max width of the shown data
    // colsSchema.forEach(col => {
    //   const bulkData = fetchedData.map(data => data.dataRow.map(d => d.fieldData).flat()).flat();
    //   const filteredBulkData = bulkData
    //     .filter(data => col.field === Object.keys(data)[0])
    //     .map(filteredData => Object.values(filteredData)[0]);
    //   if (filteredBulkData.length > 0) {
    //     const maxDataWidth = filteredBulkData.map(data => getTextWidth(data, '14pt Open Sans'));
    //     maxWidths.push(Math.max(...maxDataWidth) - 10 > 400 ? 400 : Math.max(...maxDataWidth) - 10);
    //   }
    // });
    //Template for Field validation
    const dataTemplate = (rowData, column) => {
      let field = rowData.dataRow.filter(row => Object.keys(row.fieldData)[0] === column.field)[0];
      if (field !== null && field && field.fieldValidations !== null && !isUndefined(field.fieldValidations)) {
        const validations = DataViewerUtils.orderValidationsByLevelError([...field.fieldValidations]);
        const message = DataViewerUtils.formatValidations(validations);
        const levelError = DataViewerUtils.getLevelError(validations);
        return (
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between'
            }}>
            {' '}
            {field
              ? field.fieldData.type === 'CODELIST'
                ? DataViewerUtils.parseCodelistValue(field, colsSchema)
                : field.fieldData[column.field]
              : null}{' '}
            <IconTooltip levelError={levelError} message={message} />
          </div>
        );
      } else {
        return (
          <div style={{ display: 'flex', alignItems: 'center' }}>
            {field
              ? field.fieldData.type === 'CODELIST'
                ? DataViewerUtils.parseCodelistValue(field, colsSchema)
                : field.fieldData[column.field]
              : null}
          </div>
        );
      }
    };

    //Calculate the max width of data column
    console.log({ colsSchema });
    const textMaxWidth = colsSchema.map(col => RecordUtils.getTextWidth(col.header, '14pt Open Sans'));
    const maxWidth = Math.max(...textMaxWidth) + 30;

    let columnsArr = colsSchema.map((column, i) => {
      let sort = column.field === 'id' || column.field === 'datasetPartitionId' ? false : true;
      let invisibleColumn =
        column.field === 'id' || column.field === 'datasetPartitionId' ? styles.invisibleHeader : '';
      return (
        <Column
          body={dataTemplate}
          className={invisibleColumn}
          editor={hasWritePermissions && !isWebFormMMR ? row => cellDataEditor(row, records.selectedRecord) : null}
          field={column.field}
          header={
            column.type === 'CODELIST' ? (
              <React.Fragment>
                {column.header}
                <Button
                  className={`${styles.codelistInfoButton} p-button-rounded p-button-secondary-transparent`}
                  icon="infoCircle"
                  onClick={() => {
                    const inmCodeListInfo = cloneDeep(codelistInfo);
                    console.log({ column });
                    inmCodeListInfo.name = column.codelistName;
                    inmCodeListInfo.version = column.codelistVersion;
                    inmCodeListInfo.items = column.codelistItems;
                    setCodelistInfo(inmCodeListInfo);
                    setIsCodelistInfoVisible(true);
                  }}
                  tooltip={`${column.codelistName} (${column.codelistVersion})`}
                  tooltipOptions={{ position: 'top' }}
                />
              </React.Fragment>
            ) : (
              // `${column.header}-${column.codelistName}(${column.codelistVersion})`
              column.header
            )
          }
          key={column.field}
          sortable={sort}
          style={{
            width: !invisibleColumn
              ? `${!isUndefined(maxWidths[i]) ? (maxWidth > maxWidths[i] ? maxWidth : maxWidths[i]) : maxWidth}px`
              : '0.01px'
          }}
        />
      );
    });

    let providerCode = (
      <Column
        body={providerCodeTemplate}
        className={styles.providerCode}
        header={resources.messages['countryCode']}
        key="providerCode"
        sortable={false}
        style={{ width: '100px' }}
      />
    );

    let editCol = (
      <Column
        body={row => actionTemplate(row)}
        className={styles.validationCol}
        header={resources.messages['actions']}
        key="actions"
        sortable={false}
        style={{ width: '100px' }}
      />
    );

    let validationCol = (
      <Column
        body={validationsTemplate}
        header={resources.messages['errors']}
        field="validations"
        key="recordValidation"
        sortable={false}
        style={{ width: '100px' }}
      />
    );

    if (!isDataCollection && !isWebFormMMR) {
      hasWritePermissions ? columnsArr.unshift(editCol, validationCol) : columnsArr.unshift(validationCol);
    }

    if (isDataCollection && !isWebFormMMR) {
      columnsArr.unshift(providerCode);
    }

    setColumns(columnsArr);
    setOriginalColumns(columnsArr);
    // }
  }, [colsSchema, columnOptions, records.selectedRecord, initialCellValue]);

  return {
    columns,
    setColumns,
    originalColumns
  };
};

export const useRecordErrorPosition = (
  recordErrorPositionId,
  dispatchRecords,
  records,
  dispatchSort,
  onFetchData,
  levelErrorTypesWithCorrects
) => {
  useEffect(() => {
    if (isUndefined(recordErrorPositionId) || recordErrorPositionId === -1) {
      return;
    }

    dispatchRecords({
      type: 'SET_FIRST_PAGE_RECORD',
      payload: Math.floor(recordErrorPositionId / records.recordsPerPage) * records.recordsPerPage
    });

    dispatchSort({ type: 'SORT_TABLE', payload: { order: undefined, field: undefined } });

    onFetchData(
      undefined,
      undefined,
      Math.floor(recordErrorPositionId / records.recordsPerPage) * records.recordsPerPage,
      records.recordsPerPage,
      levelErrorTypesWithCorrects
    );
  }, [recordErrorPositionId]);
};
