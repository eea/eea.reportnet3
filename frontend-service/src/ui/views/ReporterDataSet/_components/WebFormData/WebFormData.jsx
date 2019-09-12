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

    columnHeaders.map((column, i) => {
      let position = `${String.fromCharCode(97 + i).toUpperCase()}${5}`; // 5 -> still hardcoded
      formResult.push(<th name={position}>{column}</th>);
    });

    let grid = getGrid(rowHeaders, dataColumns);
    formResult.push(grid);
    return formResult;
  };

  const getGrid = (rowHeaders, columns) => {
    let grid = [];

    let firstRowPosition = columns[0][0].rowPosition;

    rowHeaders.forEach(function(element, index) {
      let columnsTds = [];
      let firstColumnPosition = columns[0][0].columnPosition;

      let headersColumnPosition = previousChar(firstColumnPosition);
      let headersRowPosition = firstRowPosition;
      let headersPosition = `${headersColumnPosition}${headersRowPosition}`;

      columnsTds.push(<td name={headersPosition}>{rowHeaders[index]}</td>);

      columns.forEach(function(column, i) {
        let position = `${firstColumnPosition}${firstRowPosition}`;
        let columnsFiltered = columns[i].filter(col => col.rowPosition === firstRowPosition);
        columnsTds.push(fillFormData(columnsFiltered, position));
        firstColumnPosition = nextChar(firstColumnPosition);
      });

      let data = fillFormData(firstRowPosition, firstColumnPosition, columns);
      grid.push(data);
      firstRowPosition++;
      grid.push(<tr>{columnsTds}</tr>);
    });

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
        <table className={styles.webFormTable}>
          <tbody>{form()}</tbody>
        </table>
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
