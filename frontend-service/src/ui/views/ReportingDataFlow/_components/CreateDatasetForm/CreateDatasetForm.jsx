import React, { useContext, useRef } from 'react';

import * as Yup from 'yup';
import { Formik, Form, Field } from 'formik';
import { isEmpty } from 'lodash';

import styles from './CreateDatasetForm.module.css';

import { Button } from 'ui/views/_components/Button';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export const CreateDatasetForm = ({ isFormReset, onCreate, setNewDatasetDialog }) => {
  const form = useRef(null);
  const resources = useContext(ResourcesContext);

  const initialValues = { datasetName: '' };
  const newDatasetValidationSchema = Yup.object().shape({
    datasetName: Yup.string().required()
  });

  if (!isFormReset) {
    form.current.resetForm();
  }
  return (
    <Formik ref={form} initialValues={initialValues} validationSchema={newDatasetValidationSchema} onSubmit={onCreate}>
      {({ errors, touched }) => (
        <Form>
          <fieldset>
            <div
              className={`${styles.form} formField${
                !isEmpty(errors.datasetName) && touched.datasetName ? ' error' : ''
              }`}>
              <Field name="datasetName" type="text" placeholder={resources.messages['createDatasetName']} />
            </div>
          </fieldset>
          <fieldset>
            <hr />
            <div className={`${styles.buttonWrap} ui-dialog-buttonpane p-clearfix`}>
              <Button
                className={
                  !isEmpty(touched)
                    ? isEmpty(errors)
                      ? styles.primaryButton
                      : styles.disabledButton
                    : styles.disabledButton
                }
                label={resources.messages['create']}
                icon="add"
                type="submit"
              />
              <Button
                className={`${styles.cancelButton} p-button-secondary`}
                label={resources.messages['cancel']}
                icon="cancel"
                onClick={() => setNewDatasetDialog(false)}
              />
            </div>
          </fieldset>
        </Form>
      )}
    </Formik>
  );
};
