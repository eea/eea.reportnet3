import React, { useContext, useState, useEffect } from 'react';

import capitalize from 'lodash/capitalize';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from '../../DataViewer.module.css';

import { config } from 'conf';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { IconTooltip } from 'ui/views/_components/IconTooltip';
import { DataViewerUtils } from '../Utils/DataViewerUtils';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { RecordUtils } from 'ui/views/_functions/Utils';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const useLoadColsSchemasAndColumnOptions = tableSchemaColumns => {
  const [columnOptions, setColumnOptions] = useState([{}]);
  const [colsSchema, setColsSchema] = useState(tableSchemaColumns);

  const resources = useContext(ResourcesContext);

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
  colsSchema,
  columnOptions,
  hasWritePermissions,
  initialCellValue,
  isDataCollection,
  isWebFormMMR,
  records,
  resources,
  setIsColumnInfoVisible,
  validationsTemplate
) => {
  const [columns, setColumns] = useState([]);
  const [originalColumns, setOriginalColumns] = useState([]);
  const [selectedHeader, setSelectedHeader] = useState();

  const onShowFieldInfo = (header, visible) => {
    setSelectedHeader(header);
    setIsColumnInfoVisible(visible);
  };

  const providerCodeTemplate = rowData => (
    <div style={{ display: 'flex', alignItems: 'center' }}>{!isUndefined(rowData) ? rowData.providerCode : null}</div>
  );

  const getFieldTypeValue = fieldType => {
    const fieldTypes = [
      { fieldType: 'Number_Integer', value: 'Number - Integer' },
      { fieldType: 'Number_Decimal', value: 'Number - Decimal' },
      { fieldType: 'Date', value: 'Date' },
      { fieldType: 'Text', value: 'Text' },
      { fieldType: 'Long_Text', value: 'Long text' },
      { fieldType: 'Email', value: 'Email' },
      { fieldType: 'URL', value: 'URL' },
      { fieldType: 'Phone', value: 'Phone number' },
      { fieldType: 'Point', value: 'Point', fieldTypeIcon: 'point' },
      { fieldType: 'Codelist', value: 'Single select' },
      { fieldType: 'Multiselect_Codelist', value: 'Multiple select' },
      { fieldType: 'Link', value: 'Link' },
      { fieldType: 'Attachement', value: 'Attachement' }
    ];

    if (!isUndefined(fieldType)) {
      const filteredTypes = fieldTypes.filter(field => field.fieldType.toUpperCase() === fieldType.toUpperCase())[0];
      return filteredTypes.value;
    } else {
      return '';
    }
  };

  const renderAttachement = () => (
    <>
      <Button
        className={`p-button-secondary-transparent`}
        icon="clipboard"
        onClick={() => {
          console.log('Download');
        }}
      />
      <Button
        className={`p-button-secondary-transparent`}
        icon="export"
        onClick={() => {
          console.log('Download');
        }}
      />
      <Button
        className={`p-button-secondary-transparent`}
        icon="trash"
        onClick={() => {
          console.log('Delete');
        }}
      />
    </>
  );

  const getTooltipMessage = column => {
    return !isNil(column) && !isNil(column.codelistItems) && !isEmpty(column.codelistItems)
      ? `<span style="font-weight:bold">Type:</span> ${getFieldTypeValue(column.type)}
        <span style="font-weight:bold">Description:</span> ${
          !isNil(column.description) && column.description !== ''
            ? column.description
            : resources.messages['noDescription']
        }<br/><span style="font-weight:bold">${
          column.type === 'CODELIST' ? resources.messages['codelists'] : resources.messages['multiselectCodelists']
        }: </span>
        ${column.codelistItems
          .map(codelistItem =>
            !isEmpty(codelistItem) && codelistItem.length > 15 ? `${codelistItem.substring(0, 15)}...` : codelistItem
          )
          .join(', ')}`
      : `<span style="font-weight:bold">Type:</span> ${getFieldTypeValue(column.type)}
      <span style="font-weight:bold">Description:</span> ${
        !isNil(column.description) && column.description !== '' && column.description.length > 35
          ? column.description.substring(0, 35)
          : isNil(column.description) || column.description === ''
          ? resources.messages['noDescription']
          : column.description
      }`;
  };

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
          {field
            ? Array.isArray(field.fieldData[column.field])
              ? field.fieldData[column.field].sort().join(', ')
              : !isNil(field.fieldData[column.field]) &&
                field.fieldData[column.field] !== '' &&
                field.fieldData.type === 'MULTISELECT_CODELIST'
              ? field.fieldData[column.field].split(',').join(', ')
              : field.fieldData.type === 'PHONE'
              ? renderAttachement()
              : field.fieldData[column.field]
            : null}
          <IconTooltip levelError={levelError} message={message} />
        </div>
      );
    } else {
      return (
        <div style={{ display: 'flex', alignItems: 'center' }}>
          {field
            ? Array.isArray(field.fieldData[column.field])
              ? field.fieldData[column.field].sort().join(', ')
              : !isNil(field.fieldData[column.field]) &&
                field.fieldData[column.field] !== '' &&
                field.fieldData.type === 'MULTISELECT_CODELIST'
              ? field.fieldData[column.field].split(',').join(', ')
              : field.fieldData.type === 'PHONE'
              ? renderAttachement()
              : field.fieldData[column.field]
            : null}
        </div>
      );
    }
  };

  useEffect(() => {
    const maxWidths = [];

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

    //Calculate the max width of data column
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
          editor={
            hasWritePermissions && !isWebFormMMR && column.type !== 'PHONE'
              ? row => cellDataEditor(row, records.selectedRecord)
              : null
          }
          field={column.field}
          header={
            <React.Fragment>
              {column.header}
              <Button
                className={`${styles.columnInfoButton} p-button-rounded p-button-secondary-transparent`}
                icon="infoCircle"
                onClick={() => {
                  onShowFieldInfo(column.header, true);
                  // setSelectedHeader(column.header);
                  // setIsColumnInfoVisible(true);
                }}
                tooltip={getTooltipMessage(column)}
                tooltipOptions={{ position: 'top' }}
              />
            </React.Fragment>
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
    getTooltipMessage,
    onShowFieldInfo,
    originalColumns,
    selectedHeader,
    setColumns
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
