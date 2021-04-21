import React, { useState, useEffect } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from '../../DataViewer.module.scss';

import { config } from 'conf';

import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { IconTooltip } from 'ui/views/_components/IconTooltip';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { DataViewerUtils } from '../Utils/DataViewerUtils';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { MapUtils } from 'ui/views/_functions/Utils/MapUtils';
import { TextUtils, RecordUtils } from 'ui/views/_functions/Utils';

export const useLoadColsSchemasAndColumnOptions = tableSchemaColumns => {
  const [colsSchema, setColsSchema] = useState(tableSchemaColumns);
  const [columnOptions, setColumnOptions] = useState([{}]);

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

export const useContextMenu = (
  resources,
  records,
  hideEdition,
  hideDeletion,
  setEditDialogVisible,
  setConfirmDeleteVisible
) => {
  const [menu, setMenu] = useState();

  useEffect(() => {
    const menuItems = [];
    if (!hideEdition) {
      menuItems.push({
        label: resources.messages['edit'],
        icon: config.icons['edit'],
        command: () => {
          setEditDialogVisible(true);
        }
      });
    }
    if (!hideDeletion) {
      menuItems.push({
        label: resources.messages['delete'],
        icon: config.icons['trash'],
        command: () => setConfirmDeleteVisible(true)
      });
    }
    setMenu(menuItems);
  }, [records.selectedRecord]);
  return { menu };
};

export const useSetColumns = (
  actionTemplate,
  cellDataEditor,
  colsSchema,
  columnOptions,
  hasCountryCode,
  hasWebformWritePermissions,
  hasWritePermissions,
  initialCellValue,
  isDataflowOpen,
  isDesignDatasetEditorRead,
  onFileDeleteVisible,
  onFileDownload,
  onFileUploadVisible,
  records,
  resources,
  setIsAttachFileVisible,
  setIsColumnInfoVisible,
  validationsTemplate,
  isReporting
) => {
  const [columns, setColumns] = useState([]);
  const [originalColumns, setOriginalColumns] = useState([]);
  const [selectedHeader, setSelectedHeader] = useState();

  const { areEquals, splitByChar } = TextUtils;

  const onShowFieldInfo = (header, visible) => {
    setSelectedHeader(header);
    setIsColumnInfoVisible(visible);
  };

  const providerCodeTemplate = rowData => (
    <div style={{ display: 'flex', alignItems: 'center' }}>{!isUndefined(rowData) ? rowData.providerCode : null}</div>
  );

  const renderAttachment = (value = '', fieldId, fieldSchemaId) => {
    const colSchema = colsSchema.filter(colSchema => colSchema.field === fieldSchemaId)[0];
    return (
      <div style={{ display: 'flex', justifyContent: 'center' }}>
        {!isNil(value) && value !== '' && (
          <Button
            className={`${value === '' && 'p-button-animated-blink'} p-button-secondary-transparent`}
            icon="export"
            iconPos="right"
            label={value}
            onClick={() => onFileDownload(value, fieldId)}
          />
        )}
        {hasWritePermissions && (!colSchema.readOnly || !isReporting) && (
          <Button
            className={`p-button-animated-blink p-button-secondary-transparent`}
            icon="import"
            onClick={() => {
              setIsAttachFileVisible(true);
              onFileUploadVisible(
                fieldId,
                fieldSchemaId,
                !isNil(colSchema) ? colSchema.validExtensions : [],
                colSchema.maxSize
              );
            }}
          />
        )}
        {hasWritePermissions && (!colSchema.readOnly || !isReporting) && !isNil(value) && value !== '' && (
          <Button
            className={`p-button-animated-blink p-button-secondary-transparent`}
            icon="trash"
            onClick={() => onFileDeleteVisible(fieldId, fieldSchemaId)}
          />
        )}
      </div>
    );
  };

  const renderPoint = (value = '') => {
    if (value !== '' && MapUtils.checkValidJSONCoordinates(value)) {
      const parsedGeoJson = JSON.parse(value);
      if (!isEmpty(parsedGeoJson.geometry.coordinates)) {
        return `${parsedGeoJson.geometry.coordinates.join(', ')} - ${parsedGeoJson.properties.srid}`;
      } else {
        return '';
      }
    }
    return '';
  };

  const renderComplexGeometries = (value = '', type) => {
    if (
      !isNil(value) &&
      value !== '' &&
      !areEquals(JSON.parse(value).geometry.type, type) &&
      MapUtils.checkValidJSONMultipleCoordinates(value)
    ) {
      const parsedGeoJson = JSON.parse(value);
      if (!isEmpty(parsedGeoJson.geometry.coordinates)) {
        return (
          <span
            style={{
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap'
            }}>{`${parsedGeoJson.geometry.coordinates.join(', ')} - ${parsedGeoJson.properties.srid}`}</span>
        );
      } else {
        return '';
      }
    }
    return '';
  };

  const getTooltipMessage = column => {
    return !isNil(column) && !isNil(column.codelistItems) && !isEmpty(column.codelistItems)
      ? `<span style="font-weight:bold">Type:</span> ${RecordUtils.getFieldTypeValue(column.type)}
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
          .join('; ')}`
      : `<span style="font-weight:bold">Type:</span> ${RecordUtils.getFieldTypeValue(column.type)}
      <span style="font-weight:bold">Description:</span> ${
        !isNil(column.description) && column.description !== '' && column.description.length > 35
          ? column.description.substring(0, 35)
          : isNil(column.description) || column.description === ''
          ? resources.messages['noDescription']
          : column.description
      }${
          column.type === 'ATTACHMENT'
            ? `<br/><span style="font-weight:bold">${resources.messages['validExtensions']}</span> ${
                !isEmpty(column.validExtensions) ? column.validExtensions.join(', ') : '*'
              }
    <span style="font-weight:bold">${resources.messages['maxFileSize']}</span> ${
                !isNil(column.maxSize) && column.maxSize.toString() !== '0'
                  ? `${column.maxSize} ${resources.messages['MB']}`
                  : resources.messages['maxSizeNotDefined']
              }`
            : ''
        }`;
  };

  const dataTemplate = (rowData, column) => {
    let field = rowData.dataRow.filter(row => Object.keys(row.fieldData)[0] === column.field)[0];
    if (!isNil(field) && !isNil(field.fieldData) && !isNil(field.fieldValidations)) {
      const validations = DataViewerUtils.orderValidationsByLevelError([...field.fieldValidations]);
      const message = DataViewerUtils.formatValidations(validations);
      const levelError = DataViewerUtils.getLevelError(validations);

      return (
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent:
              field && field.fieldData && field.fieldData.type === 'ATTACHMENT' ? 'flex-end' : 'space-between',
            whiteSpace: field && field.fieldData && field.fieldData.type === 'TEXTAREA' ? 'pre-wrap' : 'none'
          }}>
          {field
            ? Array.isArray(field.fieldData[column.field]) &&
              !['POINT', 'LINESTRING', 'POLYGON', 'MULTIPOLYGON', 'MULTILINESTRING', 'MULTIPOINT'].includes(
                field.fieldData.type
              )
              ? field.fieldData[column.field].sort().join(field.fieldData.type === 'ATTACHMENT' ? ', ' : '; ')
              : // : Array.isArray(field.fieldData[column.field])
              // ? field.fieldData[column.field].join(', ')
              (!isNil(field.fieldData[column.field]) &&
                  field.fieldData[column.field] !== '' &&
                  field.fieldData.type === 'MULTISELECT_CODELIST') ||
                (!isNil(field.fieldData[column.field]) && field.fieldData.type === 'LINK')
              ? !Array.isArray(field.fieldData[column.field])
                ? splitByChar(field.fieldData[column.field], ';')
                    .sort((a, b) => a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' }))
                    .join('; ')
                : field.fieldData[column.field]
                    .sort((a, b) => a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' }))
                    .join('; ')
              : field.fieldData.type === 'ATTACHMENT'
              ? renderAttachment(field.fieldData[column.field], field.fieldData['id'], column.field)
              : field.fieldData.type === 'POINT'
              ? renderPoint(field.fieldData[column.field])
              : ['LINESTRING', 'POLYGON', 'MULTIPOLYGON', 'MULTILINESTRING', 'MULTIPOINT'].includes(
                  field.fieldData.type
                )
              ? renderComplexGeometries(field.fieldData[column.field], field.fieldData.type)
              : field.fieldData[column.field]
            : null}
          <IconTooltip levelError={levelError} message={message} />
        </div>
      );
    } else {
      return (
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent:
              field && field.fieldData && field.fieldData.type === 'ATTACHMENT' ? 'flex-end' : 'space-between',
            whiteSpace: field && field.fieldData && field.fieldData.type === 'TEXTAREA' ? 'pre-wrap' : 'none'
          }}>
          {field
            ? Array.isArray(field.fieldData[column.field]) &&
              !['POINT', 'LINESTRING', 'POLYGON', 'MULTIPOLYGON', 'MULTILINESTRING', 'MULTIPOINT'].includes(
                field.fieldData.type
              )
              ? // ? field.fieldData[column.field].sort().join(', ')
                // : Array.isArray(field.fieldData[column.field])
                field.fieldData[column.field].join(field.fieldData.type === 'ATTACHMENT' ? ', ' : '; ')
              : (!isNil(field.fieldData[column.field]) &&
                  field.fieldData[column.field] !== '' &&
                  field.fieldData.type === 'MULTISELECT_CODELIST') ||
                (!isNil(field.fieldData[column.field]) && field.fieldData.type === 'LINK')
              ? !Array.isArray(field.fieldData[column.field])
                ? splitByChar(field.fieldData[column.field], ';')
                    .sort((a, b) => a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' }))
                    .join('; ')
                : field.fieldData[column.field]
                    .sort((a, b) => a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' }))
                    .join('; ')
              : field.fieldData.type === 'ATTACHMENT'
              ? renderAttachment(field.fieldData[column.field], field.fieldData['id'], column.field)
              : field.fieldData.type === 'POINT'
              ? renderPoint(field.fieldData[column.field])
              : ['LINESTRING', 'POLYGON', 'MULTIPOLYGON', 'MULTILINESTRING', 'MULTIPOINT'].includes(
                  field.fieldData.type
                )
              ? renderComplexGeometries(field.fieldData[column.field])
              : field.fieldData[column.field]
            : null}
        </div>
      );
    }
  };

  useEffect(() => {
    // const maxWidths = [];

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
    // const textMaxWidth = colsSchema.map(col => RecordUtils.getTextWidth(col.header, '14pt Open Sans'));
    // const maxWidth = Math.max(...textMaxWidth);

    let columnsArr = colsSchema.map((column, i) => {
      let sort = column.field === 'id' || column.field === 'datasetPartitionId' ? false : true;
      let invisibleColumn =
        column.field === 'id' || column.field === 'datasetPartitionId' ? styles.invisibleHeader : '';
      const readOnlyColumn = column.readOnly && isReporting ? styles.readOnlyFields : '';
      const headerWidth = RecordUtils.getTextWidth(column.header, '14pt Open Sans');

      return (
        <Column
          body={dataTemplate}
          className={`${invisibleColumn} ${readOnlyColumn} ${
            isDataflowOpen && isDesignDatasetEditorRead && styles.fieldDisabled
          }`}
          editor={
            hasWebformWritePermissions &&
            hasWritePermissions &&
            column.type !== 'ATTACHMENT' &&
            !isDataflowOpen &&
            !isDesignDatasetEditorRead
              ? row => cellDataEditor(row, records.selectedRecord)
              : null
          }
          field={column.field}
          header={
            <React.Fragment>
              {column.readOnly && (
                <FontAwesomeIcon
                  aria-hidden={false}
                  className="p-breadcrumb-home"
                  icon={AwesomeIcons('lock')}
                  style={{ fontSize: '8pt' }}
                />
              )}
              {column.header}
              <Button
                className={`${styles.columnInfoButton} p-button-rounded p-button-secondary-transparent datasetSchema-columnHeaderInfo-help-step`}
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
            width:
              invisibleColumn === ''
                ? `${column.readOnly ? Number(headerWidth) + 100 : Number(headerWidth) + 70}px`
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
        className={`${isDataflowOpen && isDesignDatasetEditorRead && styles.fieldDisabled}`}
        header={resources.messages['actions']}
        key="actions"
        sortable={false}
        style={{ width: '100px' }}
      />
    );

    let validationCol = (
      <Column
        body={validationsTemplate}
        className={`${isDataflowOpen && isDesignDatasetEditorRead && styles.fieldDisabled}`}
        header={resources.messages['validationsDataColumn']}
        field="validations"
        key="recordValidation"
        sortable={false}
        style={{ width: '125px' }}
      />
    );

    if (!hasCountryCode) {
      hasWritePermissions ? columnsArr.unshift(editCol, validationCol) : columnsArr.unshift(validationCol);
    }

    if (hasCountryCode) {
      columnsArr.unshift(providerCode);
    }

    if (!hasWebformWritePermissions) {
      columnsArr.splice(columnsArr.indexOf(editCol), 1);
    }

    setColumns(columnsArr);
    setOriginalColumns(columnsArr);
    // }
  }, [colsSchema, columnOptions, records.selectedRecord.recordId, initialCellValue, hasWebformWritePermissions]);

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
      payload:
        recordErrorPositionId !== -1
          ? Math.floor(recordErrorPositionId / records.recordsPerPage) * records.recordsPerPage
          : 0
    });

    dispatchSort({ type: 'SORT_TABLE', payload: { order: undefined, field: undefined } });

    onFetchData(
      undefined,
      undefined,
      recordErrorPositionId !== -1
        ? Math.floor(recordErrorPositionId / records.recordsPerPage) * records.recordsPerPage
        : 0,
      records.recordsPerPage,
      levelErrorTypesWithCorrects
    );
  }, [recordErrorPositionId]);
};
