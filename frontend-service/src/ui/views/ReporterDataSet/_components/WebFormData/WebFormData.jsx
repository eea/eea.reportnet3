import React, { useEffect, useState } from 'react';

import styles from './WebFormData.module.css';

import { Formik, Form, Field, ErrorMessage } from 'formik';
import { isEmpty, isUndefined } from 'lodash';

import { DataSetService } from 'core/services/DataSet';

export const WebFormData = ({ dataSetId, tables, tableSchemaColumns }) => {
  const getWebFormData = async () => {
    return await DataSetService.webFormDataById(dataSetId, tables[0].id);
    // return a;
  };

  const webFormData = getWebFormData();

  console.log(webFormData);
  // let form = webFormData.map((row, i) => {
  //   return <div className={styles.TabsSchema}></div>;
  // });
  // useEffect(() => {
  //   setWebFormData('ddd');
  //   onLoadWebFormData();
  //   console.log(webFormData);
  // }, []);

  return (
    <div className={`${styles.newContainer} ${styles.section}`}>
      <Formik
        initialValues={{ createSnapshotDescription: '' }}
        // validationSchema={snapshotValidationSchema}
        // onSubmit={values => {
        //   snapshotContext.snapshotDispatch({
        //     type: 'create_snapshot',
        //     payload: {
        //       description: values.createSnapshotDescription
        //     }
        //   });
        //   values.createSnapshotDescription = '';
        // }}
        render={({ errors, touched, isSubmitting }) => (
          <Form className={styles.createForm}>
            {/* {webFormData.map} */}
            <div>{/* <Field type="text" name={webFormData.records[0].recordId} placeholder={'test'} /> */}</div>
          </Form>
        )}
      />
    </div>
  );
};
