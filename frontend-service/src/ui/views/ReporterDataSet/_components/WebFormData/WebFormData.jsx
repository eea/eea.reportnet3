import React, { useContext, useEffect, useState } from 'react';

import styles from './WebFormData.module.css';

import { Formik, Form, Field, ErrorMessage } from 'formik';
import { isEmpty, isNull, isUndefined } from 'lodash';

import { Button } from 'ui/views/_components/Button';
import { InputText } from 'ui/views/_components/InputText';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

const WebFormData = ({ data }) => {
  const [mmrData, setMmrData] = useState(data.dataColumns);
  const resources = useContext(ResourcesContext);

  // useEffect(() => {
  //   setMmrData(data.dataColumns);
  // }, []);

  useEffect(() => {
    console.log(mmrData);
  }, [mmrData]);

  const form = () => {
    if (isEmpty(data)) {
      return;
    }

    let formResult = [];
    let columnHeaders = data.columnHeaders;

    let columnTitles = getColumnHeaders(columnHeaders);
    let grid = getGrid(mmrData);

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
      let i = 0;
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
                onChange={e => onEditorValueChange(filteredColumn, e.target.value)}
              />
            </td>
          );
        } else {
          tds.push(<td name={`${columnPosition}${rowIndex}`}></td>);
        }

        j++;
      }

      i++;

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
    // let updatedValue = changeRecordInTable(mmrData, getRecordId(mmrData, selectedRecord));
    // setEditDialogVisible(false);
    // setMmrData(updatedValue);
  };

  const onEditorValueChange = (props, value) => {
    // let updatedData = changeCellValue([...props.value], props.rowIndex, props.field, value);
    // console.log('onEditorValueChange props and value', props, value);
    console.log(props);
    const updatedData = changeCellValue(mmrData, value, props[0].fieldId);
    setMmrData(updatedData);
  };

  const changeCellValue = (tableData, value, fieldId) => {
    const aux = tableData.map(column =>
      column.map((col, index, originalArray) => {
        if (col.fieldId === fieldId) {
          originalArray[index].value = value;
        }
        return originalArray;
      })
    );
    console.log(aux);
    return tableData;
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
