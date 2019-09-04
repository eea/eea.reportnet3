import React, { useContext, useEffect, useState } from 'react';

import styles from './WebFormData.module.css';

import { Formik, Form, Field, ErrorMessage } from 'formik';
import { isEmpty, isUndefined } from 'lodash';

import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import { InputText } from 'ui/views/_components/InputText';

const WebFormData = ({ data }) => {
  console.log('Belgium dataset ', data);

  const webFormTable = () => {
    const title = [];
    const co2 = [];
    const f3a = [];

    const records = data.records.map(record => {
      const first = record.fields.filter(field => field.fieldSchemaId === '5d666d53460a1e0001b1671b')[0].value;
      const second = record.fields.filter(field => field.fieldSchemaId === '5d666d53460a1e0001b1672b')[0].value;
      // console.log('First   ', first);
      title.push(
        <tr>
          <td className={styles.headersLeft}>{first}</td>
          <td className={styles.tdInputFields}>
            <InputText value={second} />
          </td>
        </tr>
      );
    });

    const orderedRows = title;
    // const orderedRows = title.concat(...co2);

    return orderedRows;
  };

  return (
    <div className={`${styles.newContainer} ${styles.section}`}>
      <Formik
        initialValues={{ inputFields: '' }}
        render={({ errors, touched, isSubmitting }) => (
          <Form>
            <table>
              <tr>
                <th>1</th>
                <th>2</th>
                <th>3</th>
                <th>4</th>
                <th>5</th>
                <th>6</th>
              </tr>
              {webFormTable()}
            </table>
          </Form>
        )}
      />
    </div>
  );
};

export { WebFormData };
