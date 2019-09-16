import React, { useContext, useEffect, useState } from 'react';

import styles from './WebFormData.module.css';

import { Formik, Form, Field, ErrorMessage } from 'formik';
import { isEmpty, isNull, isUndefined } from 'lodash';

import { Button } from 'ui/views/_components/Button';
import { InputText } from 'ui/views/_components/InputText';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

const WebFormData = ({ data }) => {
  const [fetchedData, setFetchedData] = useState([]);
  const resources = useContext(ResourcesContext);

  const form = () => {
    if (isEmpty(data)) {
      return;
    }

    let formResult = [];
    let webFormData = data;
    let dataColumns = webFormData.dataColumns;
    let columnHeaders = webFormData.columnHeaders;
    let rowHeaders = webFormData.rowHeaders;

    let columnTitles = getColumnHeaders(columnHeaders);
    let grid = getGrid(rowHeaders, dataColumns);

    grid.forEach(function(element, i) {
      console.log(element);
    });

    formResult.push(
      <>
        <tr>{columnTitles}</tr>
        {grid.map(tr => (
          <div>{tr.name}</div>
        ))}
        {/* {grid} */}
      </>
    );
    console.log(grid);

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

  const getGrid = (rowHeaders, dataColumns) => {
    let grid = [];

    let rows = getMinAndMaxRows(dataColumns);
    let firstRow = parseInt(rows.firstRow);
    let lastRow = rows.lastRow;

    let columns = getMinAndMaxColumns(dataColumns);
    // let firstColumn = 0;
    // let lastColumn = dataColumns.length - 1;
    let firstColumn = columns.firstColumn.charCodeAt(0) - 64;
    let lastColumn = columns.lastColumn.charCodeAt(0) - 64;

    let header = '';

    let columnsTds = [];
    let rowsFilled = [];

    for (var rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
      let header = '';
      let tds = [];

      let i = 0;

      for (var columnIndex = firstColumn; columnIndex <= lastColumn; columnIndex++) {
        let j = 0;
        let tds = [];
        let columnPosition = String.fromCharCode(96 + columnIndex).toUpperCase();
        let header = [];

        console.log('POS: ', columnPosition, rowIndex);
        if (dataColumns[j].rowPosition === rowIndex) {
          // console.log('Pos:', dataColumns[j].rowPosition);
          if (j === 0) {
            tds.push(<td>{dataColumns[j].description}</td>);
            tds.push(
              <td name={`${columnPosition}${rowIndex}`}>
                <InputText value={dataColumns[j].value} />
              </td>
            );
          } else {
            tds.push(
              <td name={`${columnPosition}${rowIndex}`}>
                <InputText value={dataColumns[j].value} />
              </td>
            );
          }
          // header.push(<td>{dataColumns[j].description}</td>);
          tds.push(
            <td name={`${columnPosition}${rowIndex}`}>
              <InputText value={dataColumns[j].value} />
            </td>
          );
        } else {
          tds.push(
            <td name={`${columnPosition}${rowIndex}`}>
              <InputText value={''} />
            </td>
          );
        }
        j++;
      }

      rowsFilled.push(<tr>{tds}</tr>);
      console.log(rowsFilled);

      i++;
      firstRow++;
    }
    grid.push({ rowsFilled });

    return grid;
  };

  const fillFormData = (columnsFiltered, position) => {
    if (!isEmpty(columnsFiltered)) {
      let value = columnsFiltered.map(column => column.value)[0];
      let id = columnsFiltered.map(column => column.id)[0];
      return (
        <td name={position}>
          <InputText key={id} value={value} onChange={e => onEditorValueChange(columnsFiltered, e.target.value)} />
        </td>
      );
    }
  };

  const nextChar = letter => {
    return String.fromCharCode(letter.charCodeAt(0) + 1);
  };

  const previousChar = letter => {
    return String.fromCharCode(letter.charCodeAt(0) - 1);
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
    let updatedData = changeCellValue(props, value);
    // setFetchedData(updatedData);
  };

  const changeCellValue = (props, value) => {
    console.log(props, value);
    // tableData[rowIndex].dataRow.filter(data => Object.keys(data.fieldData)[0] === field)[0].fieldData[field] = value;
    // return tableData;
  };

  return (
    <div className={`${styles.newContainer} ${styles.section}`}>
      <div className="ui-dialog-buttonpane p-clearfix">
        <table className={styles.webFormTable}>{form()}</table>
        <Button label={resources.messages['cancel']} icon="cancel" onClick={onCancelRowEdit} />
        <Button
          label={resources.messages['save']}
          icon="save"
          onClick={() => {
            try {
              onSaveRecord('');
            } catch (error) {
              console.error(error);
            }
          }}
        />
      </div>
    </div>
  );
};

export { WebFormData };
