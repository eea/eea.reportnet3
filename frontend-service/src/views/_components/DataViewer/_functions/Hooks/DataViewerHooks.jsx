import { Fragment, useEffect, useState } from 'react';
import ReactDOMServer from 'react-dom/server';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './DataViewerHooks.module.scss';

import { config } from 'conf';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'views/_components/Button';
import { Column } from 'primereact/column';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IconTooltip } from 'views/_components/IconTooltip';
import { TooltipButton } from 'views/_components/TooltipButton';

import { DataViewerUtils } from '../Utils/DataViewerUtils';
import { MapUtils } from 'views/_functions/Utils/MapUtils';
import { RecordUtils } from 'views/_functions/Utils/RecordUtils';
import { TextByDataflowTypeUtils } from 'views/_functions/Utils/TextByDataflowTypeUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';
import { stubFalse } from 'lodash';

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
  dataflowType,
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
    <div className={styles.providerCode}>{!isUndefined(rowData) ? rowData.providerCode : null}</div>
  );

  const renderAttachment = (value = '', fieldId, fieldSchemaId) => {
    const colSchema = colsSchema.filter(colSchema => colSchema.field === fieldSchemaId)[0];
    return (
      <div className={styles.attachment}>
        {!isNil(value) && value !== '' && (
          <Button
            className={`${value === '' && 'p-button-animated-blink'} p-button-secondary-transparent`}
            icon="export"
            iconPos="right"
            label={value}
            onClick={() => onFileDownload(value, fieldId)}
          />
        )}
        {hasWritePermissions && !isDataflowOpen && !isDesignDatasetEditorRead && (!colSchema.readOnly || !isReporting) && (
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
        {hasWritePermissions &&
          !isDataflowOpen &&
          !isDesignDatasetEditorRead &&
          (!colSchema.readOnly || !isReporting) &&
          !isNil(value) &&
          value !== '' && (
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
          <span className={styles.complexGeometries}>{`${parsedGeoJson.geometry.coordinates.join(', ')} - ${
            parsedGeoJson.properties.srid
          }`}</span>
        );
      } else {
        return '';
      }
    }
    return '';
  };

  const getTooltipMessage = column => {
    const renderColumnType = () => {
      return (
        <div className={styles.fieldText}>
          <span>{resources.messages['type']}: </span> <br />
          <span className={styles.resourceMessage}>{RecordUtils.getFieldTypeValue(column.type)?.value}</span>
        </div>
      );
    };

    const renderColumnPK = () => {
      if (column.pk) {
        return (
          <div className={styles.fieldText}>
            <span>{resources.messages['primaryKey']}</span>
          </div>
        );
      }
    };

    const renderColumnRequired = () => {
      if (column.required) {
        return (
          <div className={styles.fieldText}>
            <span>{resources.messages['required']}</span>
          </div>
        );
      }
    };

    const renderColumnDescription = () => {
      const columnDescription = () => {
        if (!isNil(column.description) && column.description.length > 35) {
          return `${column.description.substring(0, 35)}...`;
        } else if (isNil(column.description) || column.description === '') {
          return resources.messages['noDescription'];
        } else {
          return column.description;
        }
      };

      return (
        <div className={styles.fieldText}>
          <span>{resources.messages['description']}: </span>
          <br />
          <span className={styles.resourceMessage}>{columnDescription()}</span>
        </div>
      );
    };

    const renderColumnCodeLists = () => {
      if (!isNil(column) && !isNil(column.codelistItems) && !isEmpty(column.codelistItems)) {
        return (
          <div className={styles.fieldText}>
            <span>
              {column.type === 'CODELIST'
                ? resources.messages['codelists']
                : resources.messages['multiselectCodelists']}
              :{' '}
            </span>
            <br />
            <span className={styles.resourceMessage}>
              {column.codelistItems
                .map(codelistItem =>
                  !isEmpty(codelistItem) && codelistItem.length > 15
                    ? `${codelistItem.substring(0, 15)}...`
                    : codelistItem
                )
                .join('; ')}
            </span>
          </div>
        );
      }
    };

    const renderColumnAttachment = () => {
      const columMaxSize = () => {
        if (!isNil(column.maxSize) && column.maxSize.toString() !== '0') {
          return `${column.maxSize} ${resources.messages['MB']}`;
        } else {
          return resources.messages['maxSizeNotDefined'];
        }
      };

      if (column.type === 'ATTACHMENT') {
        return (
          <Fragment>
            <br />
            <span className={styles.fieldText}>{resources.messages['validExtensions']} </span>
            <span className={styles.resourceMessage}>
              {!isEmpty(column.validExtensions)
                ? column.validExtensions.map(extension => `.${extension}`).join(', ')
                : '*'}
            </span>
            <br />
            <span className={styles.fieldText}>{resources.messages['maxFileSize']}</span>
            <span className={styles.resourceMessage}>{columMaxSize()}</span>
          </Fragment>
        );
      }
    };

    return (
      <Fragment>
        {renderColumnType()}
        {renderColumnDescription()}
        {renderColumnAttachment()}
        {renderColumnCodeLists()}
        {renderColumnPK()}
        {renderColumnRequired()}
      </Fragment>
    );
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
              : (!isNil(field.fieldData[column.field]) &&
                  field.fieldData[column.field] !== '' &&
                  field.fieldData.type === 'MULTISELECT_CODELIST') ||
                (!isNil(field.fieldData[column.field]) &&
                  (field.fieldData.type === 'LINK' || field.fieldData.type === 'EXTERNAL_LINK'))
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
              ? field.fieldData[column.field].join(field.fieldData.type === 'ATTACHMENT' ? ', ' : '; ')
              : (!isNil(field.fieldData[column.field]) &&
                  field.fieldData[column.field] !== '' &&
                  field.fieldData.type === 'MULTISELECT_CODELIST') ||
                (!isNil(field.fieldData[column.field]) &&
                  (field.fieldData.type === 'LINK' || field.fieldData.type === 'EXTERNAL_LINK'))
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
            isDataflowOpen && isDesignDatasetEditorRead ? styles.fieldDisabled : ''
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
            <Fragment>
              {column.readOnly && (
                <FontAwesomeIcon
                  aria-hidden={false}
                  className={`p-breadcrumb-home ${styles.iconSize}`}
                  icon={AwesomeIcons('lock')}
                />
              )}
              {column.header}
              <TooltipButton
                getContent={() =>
                  ReactDOMServer.renderToStaticMarkup(
                    <div className={styles.tooltipTitle}>{getTooltipMessage(column)}</div>
                  )
                }
                onClick={() => {
                  onShowFieldInfo(column.header, true);
                }}
                uniqueIdentifier={i}
              />
            </Fragment>
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
        className={styles.colWidth}
        header={TextByDataflowTypeUtils.getLabelByDataflowType(
          resources.messages,
          dataflowType,
          'dataViewerProviderCodeColumnHeader'
        )}
        key="providerCode"
        sortable={false}
      />
    );

    let editCol = (
      <Column
        body={row => actionTemplate(row)}
        className={`${styles.colWidth} ${isDataflowOpen && isDesignDatasetEditorRead ? styles.fieldDisabled : ''}`}
        header={resources.messages['actions']}
        key="actions"
        sortable={false}
      />
    );

    let validationCol = (
      <Column
        body={validationsTemplate}
        className={`${styles.validationColWidth} ${
          isDataflowOpen && isDesignDatasetEditorRead ? styles.fieldDisabled : null
        } `}
        field="validations"
        header={resources.messages['validationsDataColumn']}
        key="recordValidation"
        sortable={false}
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
  }, [
    colsSchema,
    columnOptions,
    dataflowType,
    hasWebformWritePermissions,
    hasWritePermissions,
    initialCellValue,
    records.selectedRecord.recordId
  ]);

  return {
    columns,
    getTooltipMessage,
    onShowFieldInfo,
    originalColumns,
    selectedHeader,
    setColumns
  };
};
