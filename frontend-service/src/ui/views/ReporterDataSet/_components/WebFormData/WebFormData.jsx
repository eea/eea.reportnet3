import React, { useContext, useEffect, useState } from 'react';

import styles from './WebFormData.module.css';

import { Formik, Form, Field, ErrorMessage } from 'formik';
import { isEmpty, isUndefined } from 'lodash';

import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import { InputText } from 'ui/views/_components/InputText';

const WebFormData = ({ data }) => {
  const resources = useContext(ResourcesContext);

  const [columns, setColumns] = useState([]);

  console.log('Belgium dataset ', data);

  useEffect(() => {
    const headers = [
      {
        id: 'tableSchemaName',
        header: resources.messages['origin']
      },
      {
        id: 'levelError',
        header: resources.messages['levelError']
      },
      {
        id: 'message',
        header: resources.messages['errorMessage']
      },
      {
        id: 'entityType',
        header: resources.messages['entityType']
      },
      {
        id: 'entityType2',
        header: resources.messages['entityType']
      },
      {
        id: 'entityType3',
        header: resources.messages['entityType']
      },
      {
        id: 'entityType4',
        header: resources.messages['entityType']
      }
    ];

    let columnsArr = headers.map(col => (
      <h2 key={col.id} field={col.id} header={col.header} className={styles.formColumn}>
        {col.header}
      </h2>
    ));
    columnsArr.push(<text key="recordId" field="recordId" header="" className={styles.VisibleHeader} />);
    columnsArr.push(<text key="tableSchemaId" field="tableSchemaId" header="" className={styles.VisibleHeader} />);
    setColumns(columnsArr);

    // fetchData('', sortOrder, firstRow, numRows);
  }, []);

  const recordsArray = data.records.map(recordDTO => {
    const fieldsArray = recordDTO.fields.map(fieldDTO => {
      return <InputText value={fieldDTO.value} placeholder={fieldDTO.value} className={styles.inputField} />;
    });
    return fieldsArray;
  });

  return (
    <div className={`${styles.newContainer} ${styles.section}`}>
      <Formik
        initialValues={{ inputFields: '' }}
        render={({ errors, touched, isSubmitting }) => (
          <Form>
            <div className={styles.divWebForm}>{columns}</div>
          </Form>
        )}
      />
    </div>
  );
};

export { WebFormData };
