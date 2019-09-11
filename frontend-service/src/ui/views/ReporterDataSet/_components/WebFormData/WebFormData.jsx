import React, { useContext, useEffect, useState } from 'react';

import styles from './WebFormData.module.css';

import { Formik, Form, Field, ErrorMessage } from 'formik';
import { isEmpty, isUndefined } from 'lodash';

import { InputText } from 'ui/views/_components/InputText';

const WebFormData = ({ data }) => {
  const headerFieldSchemaId = '5d666d53460a1e0001b16717';
  const valueFieldSchemaId = '5d666d53460a1e0001b16728';
  const descriptionFieldSchemaId = '5d666d53460a1e0001b1671b';
  const letterFieldSchemaId = '5d666d53460a1e0001b16721';
  const numberFieldSchemaId = '5d666d53460a1e0001b16723';

  console.log('Belgium dataset ', data);

  const getFormData = () => {
    const formData = {};
    const columnHeaders = [];
    const rows = [];
    const letters = [];
    const rowHeaders = [];

    columnHeaders.unshift('GREENHOUSE GAS SORUCE');

    const records = data.records.map((record, i) => {
      let cellValue = record.fields.filter(field => field.fieldSchemaId === valueFieldSchemaId)[0].value;
      if (cellValue === null || cellValue === undefined || cellValue === '') {
        cellValue = '';
      }

      let columnHeader = record.fields.filter(field => field.fieldSchemaId === headerFieldSchemaId)[0].value;
      if (columnHeaders.includes(columnHeader) !== true) {
        columnHeaders.push(columnHeader);
      }

      let rowHeader = record.fields.filter(field => field.fieldSchemaId === descriptionFieldSchemaId)[0].value;
      if (rowHeaders.includes(rowHeader) !== true) {
        rowHeaders.push(rowHeader);
      } else {
        console.log(rowHeader);
      }

      let columnPosition = record.fields.filter(field => field.fieldSchemaId === letterFieldSchemaId).sort()[0].value;

      let rowPosition = record.fields.filter(field => field.fieldSchemaId === numberFieldSchemaId)[0].value;

      if (letters.includes(columnPosition) !== true) {
        letters.push(columnPosition);
      }

      let row = {};
      row.rowPosition = parseInt(rowPosition);
      row.columnPosition = columnPosition;
      row.value = cellValue;
      row.header = rowHeader;

      rows.push(row);
    });

    letters.sort();
    columnHeaders.splice(10, 0, '');
    formData.columnHeaders = columnHeaders;
    let columns = createColumns(rows, letters);
    formData.rowHeaders = rowHeaders;
    formData.columns = columns;
    return formData;
  };

  const nextChar = letter => {
    return String.fromCharCode(letter.charCodeAt(0) + 1);
  };

  const previousChar = letter => {
    return String.fromCharCode(letter.charCodeAt(0) - 1);
  };

  const createColumns = (rowsData, letters) => {
    let columns = [];
    letters.forEach(function(value, index) {
      let column = rowsData.filter(function(row) {
        return row.columnPosition == value;
      });
      columns.push(column);
    });
    return columns;
  };

  const form = () => {
    let formResult = [];
    let data = getFormData();
    let columns = data.columns;
    let columnHeaders = data.columnHeaders;
    let rowHeaders = data.rowHeaders;

    columnHeaders.map((column, i) => {
      let position = `${String.fromCharCode(97 + i).toUpperCase()}${5}`; // 5 -> still hardcoded
      formResult.push(<th name={position}>{column}</th>);
    });

    let grid = getGrid(rowHeaders, columns);
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
      let values = columnsFiltered.map(column => column.value);
      return (
        <td name={position}>
          <InputText value={values[0]} />
        </td>
      );
    }
  };

  return (
    <div className={`${styles.newContainer} ${styles.section}`}>
      <table className={styles.webFormTable}>
        <tbody>{form()}</tbody>
      </table>
      <button>Save</button>
    </div>
  );
};

export { WebFormData };
