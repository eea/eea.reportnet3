import React, { useContext, useRef } from 'react';

import * as Yup from 'yup';
import { Formik, Form, Field } from 'formik';
import { isEmpty } from 'lodash';

import styles from './CreateDataflowForm.module.css';

import { Button } from 'ui/views/_components/Button';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { DataflowService } from 'core/services/Dataflow';

export const CreateDataflowForm = ({ isFormReset, onCreate, onCancel }) => {
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
      onSubmit={async (values, { setSubmitting }) => {
        setSubmitting(true);
        const response = await DataflowService.create(values.dataflowName, values.dataflowDescription);
        if (response.status >= 200 && response.status <= 299) {
          onCreate();
        } else {
          console.error(`Error in the creation of dataflow`);
        }
        setSubmitting(false);
      }}>
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
                disabled={isSubmitting}
                icon="add"
                type={isSubmitting ? '' : 'submit'}
              />
              <Button
                className={`${styles.cancelButton} p-button-secondary`}
                label={resources.messages['cancel']}
                icon="cancel"
                onClick={() => onCancel()}
              />
            </div>
          </fieldset>
        </Form>
      )}
    </Formik>
  );
};
