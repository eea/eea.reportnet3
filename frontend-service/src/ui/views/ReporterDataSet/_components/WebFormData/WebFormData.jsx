import React, { useEffect, useState } from 'react';

import styles from './WebFormData.module.css';

import { Formik, Form, Field, ErrorMessage } from 'formik';
import { isEmpty, isUndefined } from 'lodash';

const WebFormData = ({ data }) => {
  console.log(data);

  return (
    <div className={`${styles.newContainer} ${styles.section}`}>
      <Formik
        initialValues={{ createSnapshotDescription: '' }}
        render={({ errors, touched, isSubmitting }) => (
          <Form className={styles.createForm}>
            <div>
              <Field type="text" name="test" placeholder={'test'} />
            </div>
          </Form>
        )}
      />
    </div>
  );
};

export { WebFormData };
