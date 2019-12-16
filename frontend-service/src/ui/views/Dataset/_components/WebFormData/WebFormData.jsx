import React, { useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { isEmpty, isUndefined } from 'lodash';

import styles from './WebFormData.module.css';

import { InputText } from 'ui/views/_components/InputText';
import { Spinner } from 'ui/views/_components/Spinner';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { DatasetService } from 'core/services/Dataset';
import { routes } from 'ui/routes';

const WebFormData = withRouter(({ datasetId, tableSchemaId, match: { params: { dataflowId } }, history }) => {
  const [fetchedData, setFetchedData] = useState([]);
  const [initialCellValue, setInitialCellValue] = useState();
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    onLoadWebForm();
  }, []);

  const onEditorKeyChange = (props, event) => {
    if (event.key === 'Escape') {
      const updatedData = changeCellValue(fetchedData.dataColumns, initialCellValue, props.fieldId);
      setFetchedData({ ...fetchedData, dataColumns: updatedData });
    }
  };

  const onEditorSubmitValue = async (cell, value) => {
    if (!isEmpty(cell)) {
      if (value !== initialCellValue) {
        const fieldUpdated = DatasetService.updateFieldById(
          datasetId,
          cell.fieldSchemaId,
          cell.fieldId,
          cell.type,
          value
        );
        if (!fieldUpdated) {
          console.error('Error!');
        }
      }
    }
  };

  const onEditorValueChange = (props, value) => {
    const updatedData = changeCellValue(fetchedData.dataColumns, value, props.fieldId);
    setFetchedData({ ...fetchedData, dataColumns: updatedData });
  };

  const onEditorValueFocus = value => {
    setInitialCellValue(value);
  };

  const onLoadWebForm = async () => {
    try {
      const webFormData = await DatasetService.webFormDataById(datasetId, tableSchemaId);
      setFetchedData(webFormData);
    } catch (error) {
      onErrorLoadingWebForm(error);
    } finally {
      setLoading(false);
    }
  };

  const onErrorLoadingWebForm = error => {
    console.error('WebForm error: ', error);
    const errorResponse = error.response;
    console.error('WebForm errorResponse: ', errorResponse);
    if (!isUndefined(errorResponse) && (errorResponse.status === 401 || errorResponse.status === 403)) {
      history.push(getUrl(routes.DATAFLOW, { dataflowId }));
    }
  };

  const changeCellValue = (tableData, value, fieldId) => {
    tableData.map(column =>
      column.map((field, index, originalArray) => {
        if (field.fieldId === fieldId) {
          originalArray[index].value = value;
        }
        return originalArray;
      })
    );
    return tableData;
  };

  const getWebFormData = () => {
    let webFormData = fetchedData;

    if (isEmpty(webFormData)) {
      return;
    }

    let dataColumns = webFormData.dataColumns;
    let columnHeaders = webFormData.columnHeaders;

    let columnTitles = getColumnHeaders(columnHeaders);
    let webFormRows = getWebFormRows(dataColumns);

    let data = { titles: columnTitles, rows: webFormRows };

    return data;
  };

  const form = data => {
    return (
      <table className={styles.webFormTable}>
        <thead>
          <tr className={styles.columnHeaders}>{data.titles}</tr>
        </thead>
        <tbody>{data.rows}</tbody>
      </table>
    );
  };

  const webForm = () => {
    let webFormCreated = getWebFormData();
    if (isEmpty(webFormCreated)) {
      return <div></div>;
    }
    return <div>{form(webFormCreated)}</div>;
  };

  const getColumnHeaders = columnHeaders => {
    let columnsTitles = [];
    columnHeaders.map((column, i) => {
      let position = `${String.fromCharCode(97 + i).toUpperCase()}`;
      columnsTitles.push(
        <th key={`${position}${i}`} className={styles.columnTitle} name={`${position}${i}`}>
          {column}
        </th>
      );
    });
    return columnsTitles;
  };

  const getMinAndMaxRows = dataColumns => {
    let firstRow = 10000;
    let lastRow = 0;
    let rowHeaders = [];
    dataColumns.forEach(column => {
      column.forEach(field => {
        let rowPosition = parseInt(field.rowPosition);
        if (firstRow > rowPosition) {
          firstRow = rowPosition;
        }
        if (lastRow < rowPosition) {
          lastRow = rowPosition;
        }
        let description = field.description;
        if (!rowHeaders.includes(description)) {
          rowHeaders.push(description);
        }
      });
    });
    let rows = { firstRow: firstRow, lastRow: lastRow, rowHeaders: rowHeaders };
    return rows;
  };

  const getMinAndMaxColumns = dataColumns => {
    let firstColumn = 'Z';
    let lastColumn = 'A';
    dataColumns.forEach((_, i) => {
      let columnPosition = dataColumns[i][0].columnPosition;
      if (columnPosition < firstColumn) {
        firstColumn = columnPosition;
      }
      if (columnPosition > lastColumn) {
        lastColumn = columnPosition;
      }
    });
    let columns = { firstColumn: firstColumn, lastColumn: lastColumn };
    return columns;
  };

  const onFillWebFormRows = (minsAndMaxRowsAndColumns, dataColumns) => {
    let firstRow = parseInt(minsAndMaxRowsAndColumns.rows.firstRow);
    let lastRow = minsAndMaxRowsAndColumns.rows.lastRow;

    let firstColumn = minsAndMaxRowsAndColumns.columns.firstColumn.charCodeAt(0) - 64;
    let lastColumn = minsAndMaxRowsAndColumns.columns.lastColumn.charCodeAt(0) - 64;

    let headerLetterColumn = String.fromCharCode(97 + firstColumn - 2).toUpperCase();

    let filledRows = [];
    let header = '';

    for (var rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
      let j = 0;
      let tds = [];

      for (var columnIndex = firstColumn; columnIndex <= lastColumn; columnIndex++) {
        let columnPosition = String.fromCharCode(96 + columnIndex).toUpperCase();
        let filteredColumn = [];
        if (!isUndefined(dataColumns[j])) {
          filteredColumn = dataColumns[j].filter(column => column.rowPosition === rowIndex.toString());
          if (!isEmpty(filteredColumn)) {
            header = filteredColumn[0].description;
            tds.push(
              <td key={`${columnIndex}${rowIndex}`} name={`${columnPosition}${rowIndex}`}>
                <InputText
                  value={filteredColumn[0].value}
                  onBlur={e => onEditorSubmitValue(filteredColumn[0], e.target.value)}
                  onChange={e => onEditorValueChange(filteredColumn[0], e.target.value)}
                  onFocus={e => onEditorValueFocus(e.target.value)}
                  onKeyDown={e => onEditorKeyChange(filteredColumn[0], e)}
                />
              </td>
            );
          } else {
            tds.push(
              <td key={`${columnIndex}${rowIndex}`} name={`${columnPosition}${rowIndex}`}>
                <InputText className={styles.disabledInput} disabled={true} />
              </td>
            );
          }
          j++;
        }
      }
      if (!isEmpty(header))
        filledRows.push(
          <tr key={rowIndex} name={rowIndex}>
            <td
              key={`${headerLetterColumn}${rowIndex}`}
              name={`${headerLetterColumn}${rowIndex}`}
              className={styles.rowTitle}>
              {header}
            </td>
            {tds}
          </tr>
        );
      header = '';
    }
    return filledRows;
  };

  const getWebFormRows = dataColumns => {
    let webFormRows = [];
    let minAndMaxRows = getMinAndMaxRows(dataColumns);
    let minAndMaxColumns = getMinAndMaxColumns(dataColumns);
    let minsAndMaxRowsAndColumns = { rows: minAndMaxRows, columns: minAndMaxColumns };
    let filledRows = onFillWebFormRows(minsAndMaxRowsAndColumns, dataColumns);
    webFormRows.push(filledRows);
    return webFormRows;
  };

  if (loading) {
    return <Spinner className={styles.webFormSpinner} />;
  }

  return (
    <div className={styles.webFormWrapper}>
      <div>{webForm()}</div>
    </div>
  );
});

export { WebFormData };
