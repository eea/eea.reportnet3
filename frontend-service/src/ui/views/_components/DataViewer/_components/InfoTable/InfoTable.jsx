import React, { useContext } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './InfoTable.module.css';

import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { IconTooltip } from 'ui/views/_components/IconTooltip';
import { InfoTableMessages } from './_components/InfoTableMessages';

import { MapUtils } from 'ui/views/_functions/Utils/MapUtils';
import { RecordUtils } from 'ui/views/_functions/Utils';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const InfoTable = ({ data, filteredColumns, isPasting, numCopiedRecords, onDeletePastedRecord }) => {
  const resources = useContext(ResourcesContext);

  const actionTemplate = record => {
    return (
      <div className={styles.infoTableCellCorrect}>
        <Button
          disabled={isPasting}
          icon="trash"
          onClick={() => {
            onDeletePastedRecord(record.recordId);
          }}
          type="button"
        />
      </div>
    );
  };

  const checkValidCoordinates = () => {
    let isValid = true;
    data.forEach(row => {
      row.dataRow.forEach(field => {
        if (field.fieldData.type === 'POINT') {
          const value = field.fieldData[Object.keys(field.fieldData)[0]];
          if (!MapUtils.checkValidCoordinates(!isNil(value) ? value : '')) {
            isValid = false;
          }
        }
      });
    });
    return isValid;
  };

  const getMaxCharactersValueByFieldType = type => {
    const longCharacters = 20;
    const decimalCharacters = 40;
    const dateCharacters = 10;
    const textCharacters = 10000;
    const richTextCharacters = 10000;
    const emailCharacters = 256;
    const phoneCharacters = 256;
    const urlCharacters = 5000;
    const codelistTextCharacters = 10000;

    switch (type) {
      case 'NUMBER_INTEGER':
        return longCharacters;
      case 'NUMBER_DECIMAL':
        return decimalCharacters;
      case 'CODELIST':
        return codelistTextCharacters;
      case 'POINT':
        return textCharacters;
      case 'DATE':
        return dateCharacters;
      case 'TEXT':
      case 'TEXTAREA':
        return textCharacters;
      case 'RICH_TEXT':
      case 'LINK':
        return richTextCharacters;
      case 'EMAIL':
        return emailCharacters;
      case 'PHONE':
        return phoneCharacters;
      case 'URL':
        return urlCharacters;
      case 'ATTACHMENT':
        return null;
      default:
        return null;
    }
  };

  const dataTemplate = (recordData, column) => {
    let field = recordData.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
    if (isUndefined(field.fieldData[column.field])) {
      return (
        <div className={styles.infoTableCellError}>
          <br />
          <br />
          <br />
        </div>
      );
    } else {
      let value = field.fieldData[column.field];
      const valueMaxLength = getMaxCharactersValueByFieldType(field.fieldData.type);
      field.fieldData[column.field] = value.substring(0, valueMaxLength);
      value = field.fieldData[column.field];
      if (['POINT', 'LINESTRING', 'POLYGON', 'MULTILINESTRING', 'MULTIPOLYGON'].includes(field.fieldData.type)) {
        if (MapUtils.isValidJSON(value)) {
          const parsedGeoJson = JSON.parse(value);
          value = `${parsedGeoJson.geometry.coordinates.join(', ')} - ${parsedGeoJson.properties.srid}`;
        }
      }
      return <div className={styles.infoTableCellCorrect}>{field ? value : null}</div>;
    }
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
        .join(', ')}`
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
              }
          <br/><span style="font-weight:bold;color:red">${resources.messages['attachmentPaste']}</span>`
            : ''
        }`;
  };

  const getColumns = () => {
    const columnsArr = filteredColumns.map(column => {
      const fieldMaxLength = getMaxCharactersValueByFieldType(column.type);
      return (
        <Column
          body={dataTemplate}
          field={column.field}
          header={
            <React.Fragment>
              {column.header}
              <Button
                className={`${styles.columnInfoButton} p-button-rounded p-button-secondary-transparent datasetSchema-columnHeaderInfo-help-step`}
                icon="infoCircle"
                onClick={() => {}}
                tooltip={getTooltipMessage(column)}
                tooltipOptions={{ position: 'top' }}
              />
            </React.Fragment>
          }
          key={column.field}
          filterMaxLength={fieldMaxLength}
        />
      );
    });

    const editCol = (
      <Column
        key="delete"
        body={row => actionTemplate(row)}
        sortable={false}
        style={{ width: '150px', height: '45px' }}
      />
    );

    const validationCol = (
      <Column
        body={validationsTemplate}
        field="validations"
        header=""
        key="recordValidation"
        sortable={false}
        style={{ width: '15px' }}
      />
    );
    columnsArr.unshift(editCol, validationCol);
    return columnsArr;
  };

  const totalCount = (
    <span>
      {resources.messages['totalPastedRecords']} {!isUndefined(numCopiedRecords) ? data.length : 0}{' '}
    </span>
  );

  const validationsTemplate = recordData => {
    return recordData.copiedCols !== filteredColumns.length ? (
      <IconTooltip
        levelError="WARNING"
        message={
          recordData.copiedCols < filteredColumns.length
            ? resources.messages['pasteColumnErrorLessMessage']
            : resources.messages['pasteColumnErrorMoreMessage']
        }
      />
    ) : (
      !checkValidCoordinates() && (
        <IconTooltip levelError="WARNING" message={resources.messages['pasteRecordsWarningCoordinatesMessage']} />
      )
    );
  };
  return (
    <React.Fragment>
      <InfoTableMessages
        checkValidCoordinates={checkValidCoordinates}
        data={data}
        filteredColumns={filteredColumns}
        numCopiedRecords={numCopiedRecords}
      />
      {!isUndefined(data) && data.length > 0 ? (
        <DataTable
          autoLayout={true}
          className={styles.infoTableData}
          paginator={true}
          paginatorRight={totalCount}
          rows={5}
          rowsPerPageOptions={[5, 10]}
          totalRecords={numCopiedRecords}
          value={data}>
          {getColumns()}
        </DataTable>
      ) : (
        //previewPastedData()
        <div className={styles.infoTablePaste}>
          <div className={styles.infoTableItem}>
            <p>{resources.messages['pasteRecordsMessage']}</p>
          </div>
          <div className={styles.lineBreak}></div>
          <div className={styles.infoTableItem}>
            <p>{resources.messages['pasteRecordsMaxMessage']}</p>
            <p>{resources.messages['pasteRecordsCoordinatesMessage']}</p>
            <p style={{ fontStyle: 'italic' }}>{resources.messages['pasteRecordsCoordinatesStructureMessage']}</p>
          </div>
        </div>
      )}
    </React.Fragment>
  );
};
