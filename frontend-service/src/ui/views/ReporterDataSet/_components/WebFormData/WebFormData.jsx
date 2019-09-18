import React, { useContext, useEffect, useState } from 'react';

import styles from './WebFormData.module.css';

import { isEmpty, isNull, isUndefined } from 'lodash';

import { InputText } from 'ui/views/_components/InputText';
import { Spinner } from 'ui/views/_components/Spinner';

import { getUrl } from 'core/infrastructure/api/getUrl';
import { DataSetService } from 'core/services/DataSet';

const WebFormData = ({ dataSetId, tableSchemaId }) => {
  const [fetchedData, setFetchedData] = useState([]);
  const [initialCellValue, setInitialCellValue] = useState();
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    onLoadWebForm();
  }, []);

  // const onEditorEnterKeyPress = event => {
  //   if (event.key === 'Enter') {
  //     const form = event.target;
  //     console.log(form);
  //     const index = Array.prototype.indexOf.call(form, event.target);
  //     form.elements[index + 1].focus();
  //     event.preventDefault();
  //   }
  // };

  const onEditorKeyChange = (props, event) => {
    if (event.key === 'Escape') {
      const updatedData = changeCellValue(fetchedData.dataColumns, initialCellValue, props.fieldId);
      setFetchedData({ ...fetchedData, dataColumns: updatedData });
    }
    // else if (event.key === 'Enter') {
    //   onEditorEnterKeyPress(event);
    //   // onEditorSubmitValue(props, event.target.value);
    // }
  };

  const onEditorSubmitValue = async (cell, value) => {
    if (!isEmpty(cell)) {
      if (value !== initialCellValue) {
        const fieldUpdated = DataSetService.updateFieldById(
          dataSetId,
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
    setLoading(true);
    const webFormData = await DataSetService.webFormDataById(dataSetId, tableSchemaId);
    setFetchedData(webFormData);
    setLoading(false);
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

  const form = () => {
    let formResult = [];
    let webFormData = fetchedData;

    if (isEmpty(webFormData)) {
      return formResult;
    }
    let dataColumns = webFormData.dataColumns;
    let columnHeaders = webFormData.columnHeaders;

    let columnTitles = getColumnHeaders(columnHeaders);
    let grid = getGrid(dataColumns);

    formResult.push(
      <>
        <tbody>
          <tr className={styles.columnHeaders}>{columnTitles}</tr>
        </tbody>
        {grid}
      </>
    );

    return formResult;
  };

  const getColumnHeaders = columnHeaders => {
    let columnsTitles = [];
    columnHeaders.map((column, i) => {
      let position = `${String.fromCharCode(97 + i).toUpperCase()}${5}`; // 5 -> still hardcoded
      columnsTitles.push(
        <th className={styles.columnTitle} name={position}>
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
    dataColumns.forEach(function(column, i) {
      column.forEach(function(field, i) {
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
    dataColumns.forEach(function(column, i) {
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

  const getGrid = dataColumns => {
    let grid = [];

    let rows = getMinAndMaxRows(dataColumns);
    let firstRow = parseInt(rows.firstRow);
    let lastRow = rows.lastRow;

    let columns = getMinAndMaxColumns(dataColumns);
    console.log(columns);
    let firstColumn = columns.firstColumn.charCodeAt(0) - 64;
    let lastColumn = columns.lastColumn.charCodeAt(0) - 64;

    let rowsFilled = [];
    let header = '';

    for (var rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
      let j = 0;
      let tds = [];

      for (var columnIndex = firstColumn; columnIndex <= lastColumn; columnIndex++) {
        let columnPosition = String.fromCharCode(96 + columnIndex).toUpperCase();
        let filteredColumn = [];
        if (!isUndefined(dataColumns[j])) {
          filteredColumn = dataColumns[j].filter(column => column.rowPosition == rowIndex);
          if (!isEmpty(filteredColumn)) {
            header = filteredColumn[0].description;
            tds.push(
              <td key={filteredColumn[0].fieldId} name={`${columnPosition}${rowIndex}`}>
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
        rowsFilled.push(
          <tbody>
            <tr name={rowIndex}>
              <td key={rowIndex} className={styles.rowTitle}>
                {header}
              </td>
              {tds}
            </tr>
          </tbody>
        );

      header = '';
    }
    grid.push(rowsFilled);
    return grid;
  };

  if (loading) {
    return <Spinner />;
  }

  return (
    <div className={styles.webFormWrapper}>
      <div>
        <table className={styles.webFormTable}>{form()}</table>
      </div>
    </div>
  );
};

export { WebFormData };
