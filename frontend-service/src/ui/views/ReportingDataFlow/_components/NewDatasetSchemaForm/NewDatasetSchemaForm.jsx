import React, { useContext, useRef } from 'react';

import * as Yup from 'yup';
import { Formik, Form, Field } from 'formik';
import { isEmpty, isNull } from 'lodash';

import styles from './NewDatasetSchemaForm.module.css';

import { Button } from 'ui/views/_components/Button';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import { DataflowService } from 'core/services/DataFlow';

export const NewDatasetSchemaForm = ({ dataflowId, isFormReset, onCreate, onUpdateData, setNewDatasetDialog }) => {
  const form = useRef(null);
  const resources = useContext(ResourcesContext);

  const initialValues = { datasetSchemaName: '' };
  const newDatasetValidationSchema = Yup.object().shape({
    datasetSchemaName: Yup.string().required()
  });

  if (!isNull(form.current)) {
    document.getElementById('dataSchemaInput').focus();
  }

  if (!isFormReset && !isNull(form.current)) {
    form.current.resetForm();
  }
  return (
    <Formik
      ref={form}
      initialValues={initialValues}
      validationSchema={newDatasetValidationSchema}
      onSubmit={async (values, { setSubmitting }) => {
        setSubmitting(true);
        const response = await DataflowService.newEmptyDatasetSchema(dataflowId, values.datasetSchemaName);
        onCreate();
        if (response === 200) {
          onCreate();
          onUpdateData();
          setSubmitting(false);
        } else {
          setSubmitting(false);
        }
      }}>
      {({ errors, touched }) => (
        <Form>
          <fieldset>
            <div
              className={`formField${!isEmpty(errors.datasetSchemaName) && touched.datasetSchemaName ? ' error' : ''}`}>
              <Field
                id="dataSchemaInput"
                name="datasetSchemaName"
                placeholder={resources.messages['createdatasetSchemaName']}
                type="text"
              />
            </div>
          </fieldset>
          <fieldset>
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
