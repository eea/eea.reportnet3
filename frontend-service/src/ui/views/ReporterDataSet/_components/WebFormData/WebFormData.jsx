import React, { useContext, useEffect, useState } from 'react';

import styles from './WebFormData.module.css';

import { isEmpty, isNull, isUndefined } from 'lodash';

import { InputText } from 'ui/views/_components/InputText';
import { Spinner } from 'ui/views/_components/Spinner';

import { DataSetService } from 'core/services/DataSet';

const WebFormData = ({ dataSetId, tableSchemaId }) => {
  const [fetchedData, setFetchedData] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    onLoadWebForm();
  }, []);

  const onLoadWebForm = async () => {
    setLoading(true);
    const webFormData = await DataSetService.webFormDataById(dataSetId, tableSchemaId);
    setFetchedData(webFormData);
    setLoading(false);
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
          <tr>{columnTitles}</tr>
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
      columnsTitles.push(<th name={position}>{column}</th>);
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
        let filteredColumn = [];
        if (!isUndefined(dataColumns[j])) {
          filteredColumn = dataColumns[j].filter(column => column.rowPosition == rowIndex);
          if (!isEmpty(filteredColumn)) {
            header = filteredColumn[0].description;
            tds.push(
              <td key={filteredColumn[0].fieldId} name={`${columnPosition}${rowIndex}`}>
                <InputText
                  value={filteredColumn[0].value}
                  onChange={e => onEditorValueChange(filteredColumn, e.target.value)}
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
              <td key={rowIndex}>{header}</td>
              {tds}
            </tr>
          </tbody>
        );

      header = '';
    }
    grid.push(rowsFilled);
    return grid;
  };

  const onSaveRecord = async record => {
    //Delete hidden column null values (recordId, validations, etc.)
    // record.dataRow = record.dataRow.filter(column => !isNull(Object.values(column.fieldData)[0]));
    // if (isNewRecord) {
    //   try {
    //     await DataSetService.addRecordsById(dataSetId, tableId, [record]);
    //     setAddDialogVisible(false);
    //     onRefresh();
    //   } catch (error) {
    //     console.error('DataViewer error: ', error);
    //     const errorResponse = error.response;
    //     console.error('DataViewer errorResponse: ', errorResponse);
    //     if (!isUndefined(errorResponse) && (errorResponse.status === 401 || errorResponse.status === 403)) {
    //       history.push(getUrl(config.REPORTING_DATAFLOW.url, { dataFlowId }));
    //     }
    //   } finally {
    //     setLoading(false);
    //   }
    // } else {
    //   try {
    //     await DataSetService.updateRecordsById(dataSetId, record);
    //     setEditDialogVisible(false);
    //     onRefresh();
    //   } catch (error) {
    //     console.error('DataViewer error: ', error);
    //     const errorResponse = error.response;
    //     console.error('DataViewer errorResponse: ', errorResponse);
    //     if (!isUndefined(errorResponse) && (errorResponse.status === 401 || errorResponse.status === 403)) {
    //       history.push(getUrl(config.REPORTING_DATAFLOW.url, { dataFlowId }));
    //     }
    //   } finally {
    //     onCancelRowEdit();
    //     setLoading(false);
    //   }
    // }
  };

  const onCancelRowEdit = () => {
    // let updatedValue = changeRecordInTable(fetchedData, getRecordId(fetchedData, selectedRecord));
    // setEditDialogVisible(false);
    // setFetchedData(updatedValue);
  };

  const onEditorValueChange = (props, value) => {
    const updatedData = changeCellValue(fetchedData.dataColumns, value, props[0].fieldId);
    setFetchedData({ ...fetchedData, dataColumns: updatedData });
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
