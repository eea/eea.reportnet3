import React, { useContext, useEffect, useState } from 'react';

import styles from './WebFormData.module.css';

import { Formik, Form, Field, ErrorMessage } from 'formik';
import { isEmpty, isUndefined } from 'lodash';

import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import { InputText } from 'ui/views/_components/InputText';
import { Column } from 'primereact/column';

const WebFormData = ({ data }) => {
  const headerFieldSchemaId = '5d666d53460a1e0001b16717';
  const valueFieldSchemaId = '5d666d53460a1e0001b16728';
  const descriptionFieldSchemaId = '5d666d53460a1e0001b1671b';
  const letterFieldSchemaId = '5d666d53460a1e0001b16721';
  const numberFieldSchemaId = '5d666d53460a1e0001b16723';

  console.log('Belgium dataset ', data);

  const getFormData = () => {
    const formData = {};
    const headersTop = [];
    const rows = [];
    const letters = [];
    const rowHeaders = [];
    const numbers = [];
    headersTop.unshift('Column1');

    const records = data.records.map((record, i) => {
      let cellValue = record.fields.filter(field => field.fieldSchemaId === valueFieldSchemaId)[0].value;
      if (cellValue === null || cellValue === undefined || cellValue === '') {
        cellValue = '';
      }

      let columnHeader = record.fields.filter(field => field.fieldSchemaId === headerFieldSchemaId)[0].value;
      let rowHeader = record.fields.filter(field => field.fieldSchemaId === descriptionFieldSchemaId)[0].value;
      let numberColumnPosition = record.fields.filter(field => field.fieldSchemaId === letterFieldSchemaId).sort()[0]
        .value;
      let rowPosition = record.fields.filter(field => field.fieldSchemaId === numberFieldSchemaId)[0].value;

      if (letters.includes(numberColumnPosition) !== true) {
        letters.push(numberColumnPosition);
      }

      if (numbers.includes(rowPosition) !== true) {
        numbers.push(rowPosition, cellValue);
      }

      let letterPosition =
        record.fields
          .filter(field => field.fieldSchemaId === letterFieldSchemaId)[0]
          .value.toLowerCase()
          .charCodeAt(0) -
        97 +
        1;

      let row = {};
      row.rowPosition = parseInt(rowPosition);
      row.columnPosition = numberColumnPosition;
      row.value = cellValue;
      row.header = rowHeader;

      if (headersTop.includes(columnHeader) !== true) {
        headersTop.push(columnHeader);
      }

      if (rowHeaders.includes(rowHeader) !== true) {
        rowHeaders.push(rowHeader);
      }

      rows.push(row);
    });
    headersTop.splice(10, 0, '');

    formData.headersTop = headersTop;
    letters.sort();
    let columns = createColumns(rows, letters);
    formData.rowHeaders = rowHeaders;
    formData.columns = columns;
    return formData;
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
    let headersTop = data.headersTop;
    let rowHeaders = data.rowHeaders;

    headersTop.map((column, i) => {
      formResult.push(<th name={`${String.fromCharCode(97 + i)}${5}`}>{column}</th>);
    });

    rowHeaders.forEach(function(element, index) {
      formResult.push(<tr>{getRowsResult(columns, index, rowHeaders)}</tr>);
    });
    return formResult;
  };

  const getRowsResult = (columns, index, rowHeaders) => {
    let result = [];
    let firstRow = columns[0][0].rowPosition;

    result.push(<td name={columns[0][0].rowPosition}>{rowHeaders[index]}</td>);
    columns.forEach(function(column, i) {
      if (isUndefined(column[index])) {
        result.push(
          <td>
            <span></span>
          </td>
        );
      } else {
        let name = `${column[index].columnPosition}${column[index].rowPosition}`;
        console.log('Fila: ', index);
        console.log('Columna: ', i);
        console.log('Valor: ', column[index].value);
        console.log('Valor Fila: ', column[index].rowPosition);
        console.log('Valor Columnas: ', column[index].columnPosition);
        result.push(
          <td name={name}>
            <InputText name={name} value={column[index].value} />
          </td>
        );
      }
    });

    return result;
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
