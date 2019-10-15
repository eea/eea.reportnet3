import React, { useContext, useRef } from 'react';

import * as Yup from 'yup';
import { Formik, Form, Field } from 'formik';
import { isEmpty } from 'lodash';

import styles from './CreateDataflowForm.module.css';

import { Button } from 'ui/views/_components/Button';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export const CreateDataflowForm = ({ isFormReset, onCreate }) => {
  const form = useRef(null);
  const resources = useContext(ResourcesContext);
  const initialValues = { dataflowName: '', dataflowDescription: '', associatedObligation: '' };
  const createDataflowValidationSchema = Yup.object().shape({
    dataflowName: Yup.string().required(),
    dataflowDescription: Yup.string().required()
  });

  if (!isFormReset) {
    form.current.resetForm();
  }

  return (
    <Formik
      ref={form}
      initialValues={initialValues}
      validationSchema={createDataflowValidationSchema}
      onSubmit={onCreate}>
      {({ isSubmitting, errors, touched }) => (
        <Form>
          <fieldset>
            <div className={`formField${!isEmpty(errors.dataflowName) && touched.dataflowName ? ' error' : ''}`}>
              <Field name="dataflowName" type="text" placeholder={resources.messages['createDataflowName']} />
            </div>
            <div
              className={`formField${
                !isEmpty(errors.dataflowDescription) && touched.dataflowDescription ? ' error' : ''
              }`}>
              <Field
                name="dataflowDescription"
                component="textarea"
                placeholder={resources.messages['createDataflowDescription']}
              />
            </div>
            <div className={styles.search}>
              <Field
                className={styles.searchInput}
                disabled={true}
                name="associatedObligation"
                placeholder={resources.messages['associatedObligation']}
                type="text"
              />
              <Button
                className={styles.searchButton}
                disabled={true}
                icon="search"
                label={resources.messages['search']}
                layout="simple"
              />
            </div>
          </fieldset>
          <fieldset>
            <div className={styles.wrapButtons}>
              <Button
                className={styles.submitButton}
                disabled={isSubmitting}
                label={resources.messages['createNewDataflow']}
                layout="simple"
                type="submit"
              />
              <Button className={styles.resetButton} label={resources.messages['reset']} layout="simple" type="reset" />
            </div>
          </fieldset>
        </Form>
      )}
    </Formik>
  );
};
