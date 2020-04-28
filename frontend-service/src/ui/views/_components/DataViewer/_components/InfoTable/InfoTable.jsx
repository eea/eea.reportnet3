import React, { useContext } from 'react';

import { isUndefined } from 'lodash';

import styles from './InfoTable.module.css';

import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { IconTooltip } from 'ui/views/_components/IconTooltip';
import { InfoTableMessages } from './_components/InfoTableMessages';

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

  const getMaxCharactersValueByFieldType = type => {
    const longCharacters = 20;
    const decimalCharacters = 40;
    const dateCharacters = 10;
    const textCharacters = 5000;
    const longTextCharacters = 10000;
    const emailCharacters = 256;
    const phoneCharacters = 256;
    const urlCharacters = 5000;

    switch (type) {
      case 'NUMBER_INTEGER':
        return longCharacters;
      case 'NUMBER_DECIMAL':
        return decimalCharacters;
      case 'POINT':
      case 'COORDINATE_LONG':
      case 'COORDINATE_LAT':
        return textCharacters;
      case 'DATE':
        return dateCharacters;
      case 'TEXT':
        return textCharacters;
      case 'LONG_TEXT':
        return longTextCharacters;
      case 'EMAIL':
        return emailCharacters;
      case 'PHONE':
        return phoneCharacters;
      case 'URL':
        return urlCharacters;
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
      return <div className={styles.infoTableCellCorrect}>{field ? value : null}</div>;
    }
  };

  const getColumns = () => {
    const columnsArr = filteredColumns.map(column => {
      const fieldMaxLength = getMaxCharactersValueByFieldType(column.type);
      return (
        <Column
          body={dataTemplate}
          field={column.field}
          header={column.header}
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
    ) : null;
  };

  return (
    <React.Fragment>
      <InfoTableMessages data={data} filteredColumns={filteredColumns} numCopiedRecords={numCopiedRecords} />
      {!isUndefined(data) && data.length > 0 ? (
        <DataTable
          className={styles.infoTableData}
          value={data}
          autoLayout={true}
          paginator={true}
          paginatorRight={totalCount}
          rowsPerPageOptions={[5, 10]}
          rows={5}
          totalRecords={numCopiedRecords}>
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
          </div>
        </div>
      )}
    </React.Fragment>
  );
};
