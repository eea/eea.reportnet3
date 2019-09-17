import React, { useContext, useEffect, useState } from 'react';

import styles from './WebFormData.module.css';

import { Formik, Form, Field, ErrorMessage } from 'formik';
import { isEmpty, isNull, isUndefined } from 'lodash';

import { Button } from 'ui/views/_components/Button';
import { InputText } from 'ui/views/_components/InputText';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';

import { getUrl } from 'core/infrastructure/api/getUrl';
import { DataSetService } from 'core/services/DataSet';

const WebFormData = ({ dataSetId, tableSchemaId }) => {
  const [fetchedData, setFetchedData] = useState([]);
  const [initialCellValue, setInitialCellValue] = useState();
  const [loading, setLoading] = useState(false);

  const resources = useContext(ResourcesContext);

  useEffect(() => {
    onLoadWebForm();
  }, []);

  const onEditorKeyChange = (props, event, record) => {
    if (event.key === 'Escape') {
      onEditorValueChange(props, event.target.value);
      // setFetchedData(updatedData);
    } else if (event.key === 'Enter' || event.key === 'Tab') {
      onEditorSubmitValue(props, event.target.value, record);
    }
  };

  const onEditorSubmitValue = async (cell, value) => {
    console.log(cell);
    if (!isEmpty(cell)) {
      //let field = fetchedData.dataColumns.reduce(column => column.filter(col => col.fieldId === cell[0].fieldId))[0];
      //record.dataRow.filter(row => Object.keys(row.fieldData)[0] === cell.field)[0].fieldData;
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
    const updatedData = changeCellValue(fetchedData.dataColumns, value, props[0].fieldId);
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
        <tr>{columnTitles}</tr>
        {grid}
      </>
    );

    return formResult;
  };

  const getColumnHeaders = columnHeaders => {
    let columnsTitles = [];
    columnHeaders.map((column, i) => {
      let position = `${String.fromCharCode(97 + i).toUpperCase()}${5}`; // 5 -> still hardcoded
      if (i === 10) {
        columnsTitles.push(
          <th name={position} className={styles.kColumn}>
            {column}
          </th>
        );
      } else {
        columnsTitles.push(<th name={position}>{column}</th>);
      }
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
    let firstColumn = dataColumns[0][0].columnPosition;
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
    console.log(dataColumns);
    let grid = [];

    let rows = getMinAndMaxRows(dataColumns);
    let firstRow = parseInt(rows.firstRow);
    let lastRow = rows.lastRow;

    let columns = getMinAndMaxColumns(dataColumns);
    let firstColumn = columns.firstColumn.charCodeAt(0) - 64;
    let lastColumn = columns.lastColumn.charCodeAt(0) - 64;

    let rowsFilled = [];
    let header = '';

    for (var rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
      let j = 0;
      let tds = [];

      for (var columnIndex = firstColumn; columnIndex <= lastColumn; columnIndex++) {
        let columnPosition = String.fromCharCode(96 + columnIndex).toUpperCase();
        let filteredColumn = dataColumns[j].filter(column => column.rowPosition == rowIndex);
        if (!isEmpty(filteredColumn)) {
          header = filteredColumn[0].description;
          tds.push(
            <td name={`${columnPosition}${rowIndex}`}>
              <InputText
                value={filteredColumn[0].value}
                onBlur={e => onEditorSubmitValue(filteredColumn, e.target.value)}
                onChange={e => onEditorValueChange(filteredColumn, e.target.value)}
                onFocus={e => onEditorValueFocus(e.target.value)}
                onKeyDown={e => onEditorKeyChange(filteredColumn, e)}
              />
            </td>
          );
        } else {
          tds.push(
            <td name={`${columnPosition}${rowIndex}`}>{/* <InputText className={styles.disabledInput} /> */}</td>
          );
        }
        j++;
      }

      if (!isEmpty(header))
        rowsFilled.push(
          <tr name={rowIndex}>
            <td>{header}</td>
            {tds}
          </tr>
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
    <div className={`${styles.newContainer} ${styles.section}`}>
      <div className="ui-dialog-buttonpane p-clearfix">
        <table className={styles.webFormTable}>{form()}</table>
      </div>
    </div>
  );
};

export { WebFormData };
