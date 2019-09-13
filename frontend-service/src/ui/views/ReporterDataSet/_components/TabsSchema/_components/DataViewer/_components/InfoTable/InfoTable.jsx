import React, { useContext } from 'react';

import { isUndefined } from 'lodash';

import styles from './InfoTable.module.css';

import { Button } from 'ui/views/_components/Button';
import { IconTooltip } from '../IconTooltip';
import { InfoTableMessages } from './_components/InfoTableMessages';

import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export const InfoTable = ({ data, columns, onDeletePastedRecord }) => {
  const resources = useContext(ResourcesContext);
  const previewPastedData = () => {
    return (
      <div className="p-datatable-wrapper">
        <table className="p-datatable" style={{ width: '100%' }}>
          <thead className="p-datatable-thead">
            <tr>{previewPastedDataHeaders()}</tr>
          </thead>
          <tbody className="p-datatable-tbody">{previewPastedDataBody()}</tbody>
        </table>
      </div>
    );
  };

  const previewPastedDataHeaders = () => {
    if (!isUndefined(data) && !isUndefined(columns)) {
      if (data.length > 0) {
        const filteredColumns = columns.filter(
          column =>
            column.key !== 'actions' &&
            column.key !== 'recordValidation' &&
            column.key !== 'id' &&
            column.key !== 'dataSetPartitionId'
        );

        const headers = filteredColumns.map((column, i) => {
          return (
            <th key={i} className="p-resizable-column">
              {column.props.header}
            </th>
          );
        });
        let deleteCol = <th key="deleteRecord" className="p-resizable-column"></th>;
        let validationCol = <th key="validationRecord" className="p-resizable-column"></th>;
        headers.unshift(deleteCol, validationCol);
        return headers;
      }
    }
  };

  const previewPastedDataBody = () => {
    if (!isUndefined(data) && !isUndefined(columns)) {
      if (data.length > 0) {
        const filteredColumns = columns.filter(
          column =>
            column.key !== 'actions' &&
            column.key !== 'recordValidation' &&
            column.key !== 'id' &&
            column.key !== 'dataSetPartitionId'
        );
        console.log(filteredColumns);
        let records = [...data];
        if (!isUndefined(records)) {
          records = records.map((record, i) => {
            return (
              <tr key={i} className="p-datatable-row">
                <td id="deleteRecord">
                  <Button
                    type="button"
                    icon="trash"
                    onClick={e => {
                      onDeletePastedRecord(i);
                    }}
                  />
                </td>
                <td>
                  {record.copiedCols !== filteredColumns.length ? (
                    <IconTooltip
                      levelError="WARNING"
                      message={
                        record.copiedCols < filteredColumns.length
                          ? resources.messages['pasteColumnErrorLessMessage']
                          : resources.messages['pasteColumnErrorMoreMessage']
                      }
                    />
                  ) : null}
                </td>
                {record.dataRow.map((column, j) => {
                  return (
                    <td
                      key={j}
                      className={isUndefined(Object.values(column.fieldData)[0]) ? styles.infoTableCellError : ''}>
                      {Object.values(column.fieldData)[0]}
                    </td>
                  );
                })}
              </tr>
            );
          });
        }
        return records;
      }
    }
  };

  return (
    <React.Fragment>
      <InfoTableMessages data={data} columns={columns} />
      <hr />
      {!isUndefined(data) && data.length > 0 ? (
        previewPastedData()
      ) : (
        <div className={styles.infoTablePaste}>
          <span>{resources.messages['pasteRecordsMessage']}</span>
        </div>
      )}
    </React.Fragment>
  );
};
