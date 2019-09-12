import React, { useState, useEffect } from 'react';

import { isUndefined } from 'lodash';

import styles from './InfoTable.module.css';

import { Button } from 'ui/views/_components/Button';
import { InfoTableMessages } from './_components/InfoTableMessages';

export const InfoTable = ({ data, columns, onDeletePastedRecord }) => {
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
        //let slicedColumns = filteredColumns.slice(0, recordsPreviewNumber);

        const headers = filteredColumns.map((column, i) => {
          return (
            <th key={i} className="p-resizable-column">
              {column.props.header}
            </th>
          );
        });
        // if (filteredColumns.length > columnsPreviewNumber) {
        //   headers.push(<th key="previewColumn">...</th>);
        // }
        let deleteCol = <th key="deleteRecord" className="p-resizable-column"></th>;
        headers.unshift(deleteCol);
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
        let records = [...data];
        //data.slice(0, recordsPreviewNumber);
        if (records) {
          records = records.map((record, i) => {
            return (
              <tr key={i} className="p-datatable-row">
                <td id="deleteRecord">
                  <Button
                    type="button"
                    icon="trash"
                    // classNamonDeletePastedRecorde={`${`p-button-rounded p-button-secondary ${styles.deleteRowButton}`}`}
                    onClick={e => {
                      onDeletePastedRecord(i);
                    }}
                  />
                </td>
                {record.dataRow
                  //   .slice(
                  //     0,
                  //     filteredColumns.length < columnsPreviewNumber ? filteredColumns.length : columnsPreviewNumber
                  //   )
                  .map((column, j) => {
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
        // if (data.length > recordsPreviewNumber) {
        //   records.push(
        //     <tr key="preview">
        //       <td>...</td>
        //     </tr>
        //   );
        // }
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
          {' '}
          <span>Paste here your data (Ctrl + V or ⌘ + V)</span>
        </div>
      )}
    </React.Fragment>
  );
};
