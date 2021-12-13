import { Fragment, useContext } from 'react';
import ReactDOMServer from 'react-dom/server';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './InfoTable.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'views/_components/Button';
import { Column } from 'primereact/column';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { DataTable } from 'views/_components/DataTable';
import { IconTooltip } from 'views/_components/IconTooltip';
import { InfoTableMessages } from './_components/InfoTableMessages';
import { TooltipButton } from 'views/_components/TooltipButton/TooltipButton';

import { MapUtils } from 'views/_functions/Utils/MapUtils';
import { RecordUtils } from 'views/_functions/Utils';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const InfoTable = ({ data, filteredColumns, isPasting, numCopiedRecords, onDeletePastedRecord }) => {
  const resourcesContext = useContext(ResourcesContext);

  const actionTemplate = record => {
    return (
      <div className={styles.infoTableCellCorrect}>
        <Button
          className={`p-button-rounded p-button-secondary-transparent ${styles.deleteRowButton}`}
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
    const datetimeCharacters = 20;
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
      case 'DATETIME':
        return datetimeCharacters;
      case 'TEXT':
      case 'TEXTAREA':
        return textCharacters;
      case 'RICH_TEXT':
      case 'EXTERNAL_LINK':
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
      if (
        ['POINT', 'LINESTRING', 'POLYGON', 'MULTILINESTRING', 'MULTIPOLYGON', 'MULTIPOINT'].includes(
          field.fieldData.type
        )
      ) {
        if (MapUtils.isValidJSON(value)) {
          const parsedGeoJson = JSON.parse(value);
          value = `${parsedGeoJson.geometry.coordinates.join(', ')} - ${parsedGeoJson.properties.srid}`;
        }
      }
      return <div className={styles.infoTableCellCorrect}>{field ? value : null}</div>;
    }
  };

  const getTooltipMessage = column => {
    const renderColumnType = () => {
      return (
        <div className={styles.fieldText}>
          <span>{resourcesContext.messages['type']}: </span>
          <br />
          <span className={styles.propertyLabel}>{RecordUtils.getFieldTypeValue(column.type)?.value}</span>
        </div>
      );
    };

    const renderColumnPK = () => {
      if (column.pk) {
        return (
          <div className={`${styles.fieldText} ${styles.fieldTextPaddingTop}`}>
            <span className={styles.propertyLabel}>{resourcesContext.messages['primaryKey']}</span>
          </div>
        );
      }
    };

    const renderColumnRequired = () => {
      if (column.required) {
        return (
          <div className={`${styles.fieldText} ${styles.fieldTextPaddingTop}`}>
            <span className={styles.propertyLabel}>{resourcesContext.messages['required']}</span>
          </div>
        );
      }
    };

    const renderColumnDescription = () => {
      const columnDescription = () => {
        if (column.description.length > 35) {
          return `${column.description.substring(0, 35)}...`;
        } else {
          return column.description;
        }
      };
      if (!isNil(column.description) && column.description.trim() !== '') {
        return (
          <div className={`${styles.fieldText} ${styles.fieldTextPaddingTop}`}>
            <span>{resourcesContext.messages['description']}: </span>
            <br />
            <span className={styles.propertyLabel}>{columnDescription()}</span>
          </div>
        );
      }
    };

    const renderColumnCodeLists = () => {
      if (!isNil(column) && !isNil(column.codelistItems) && !isEmpty(column.codelistItems)) {
        return (
          <div className={`${styles.fieldText} ${styles.fieldTextPaddingTop}`}>
            <span>
              {column.type === 'CODELIST'
                ? resourcesContext.messages['codelists']
                : resourcesContext.messages['multiselectCodelists']}
              :{' '}
            </span>
            <br />
            <span
              className={`${styles.propertyLabel} ${
                column.codelistItems.length > 15 ? styles.propertyLabelListItems : ''
              }`}>
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
          return `${column.maxSize} ${resourcesContext.messages['MB']}`;
        } else {
          return resourcesContext.messages['maxSizeNotDefined'];
        }
      };

      if (column.type === 'ATTACHMENT') {
        return (
          <Fragment>
            <span className={`${styles.fieldText} ${styles.fieldTextPaddingTop}`}>
              {resourcesContext.messages['validExtensions']}{' '}
            </span>
            <span className={styles.propertyLabel}>
              {!isEmpty(column.validExtensions)
                ? column.validExtensions.map(extension => `.${extension}`).join(', ')
                : '*'}
            </span>
            <span className={`${styles.fieldText} ${styles.fieldTextPaddingTop}`}>
              {resourcesContext.messages['maxFileSize']}
            </span>
            <span className={styles.propertyLabel}>{columMaxSize()}</span>
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

  const getColumns = () => {
    const columnsArr = filteredColumns.map((column, i) => {
      const fieldMaxLength = getMaxCharactersValueByFieldType(column.type);
      return (
        <Column
          body={dataTemplate}
          field={column.field}
          filterMaxLength={fieldMaxLength}
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
                    <div
                      style={{
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'flex-start',
                        opacity: '0.9'
                      }}>
                      {getTooltipMessage(column)}
                    </div>
                  )
                }
                uniqueIdentifier={i}
              />
            </Fragment>
          }
          key={column.field}
        />
      );
    });

    const editCol = (
      <Column
        body={row => actionTemplate(row)}
        key="delete"
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
      {resourcesContext.messages['totalPastedRecords']} {!isUndefined(numCopiedRecords) ? data.length : 0}{' '}
    </span>
  );

  const validationsTemplate = recordData => {
    return recordData.copiedCols !== filteredColumns.length ? (
      <IconTooltip
        levelError="WARNING"
        message={
          recordData.copiedCols < filteredColumns.length
            ? resourcesContext.messages['pasteColumnErrorLessMessage']
            : resourcesContext.messages['pasteColumnErrorMoreMessage']
        }
      />
    ) : (
      !checkValidCoordinates() && (
        <IconTooltip
          levelError="WARNING"
          message={resourcesContext.messages['pasteRecordsWarningCoordinatesMessage']}
        />
      )
    );
  };
  return (
    <Fragment>
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
        <div className={styles.infoTablePaste}>
          <div className={styles.infoTableItem}>
            <p>{resourcesContext.messages['pasteRecordsMessage']}</p>
          </div>
          <div className={styles.lineBreak}></div>
          <div className={styles.infoTableItem}>
            <p>{resourcesContext.messages['pasteRecordsMaxMessage']}</p>
            <p>{resourcesContext.messages['pasteRecordsCoordinatesMessage']}</p>
            <p style={{ fontStyle: 'italic' }}>
              {resourcesContext.messages['pasteRecordsCoordinatesStructureMessage']}
            </p>
          </div>
        </div>
      )}
    </Fragment>
  );
};
