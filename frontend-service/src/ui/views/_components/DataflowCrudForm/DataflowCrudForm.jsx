import React, { useContext, useEffect, useRef } from 'react';

import * as Yup from 'yup';
import { Formik, Form, Field } from 'formik';
import { isEmpty, isNull, isPlainObject, isUndefined, sortBy } from 'lodash';

import styles from './DataflowCrudForm.module.css';

import { Button } from 'ui/views/_components/Button';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { DataflowService } from 'core/services/Dataflow';

export const DataflowCrudForm = ({ dataflowValue, isDialogVisible, isEditForm, isFormReset, onCreate, onCancel }) => {
  const resources = useContext(ResourcesContext);

  const form = useRef(null);
  const inputRef = useRef();

  useEffect(() => {
    if (isDialogVisible) {
      if (!isUndefined(inputRef)) {
        inputRef.current.focus();
      }
    }
  }, [isDialogVisible]);

  const dataflowCrudValidation = Yup.object().shape({
    dataflowName: Yup.string().required(),
    dataflowDescription: Yup.string().required()
  });

  if (!isFormReset) {
    form.current.resetForm();
  }

  const buildFormikValues = selectedValues => {
    const formValues = isEditForm ? selectedValues : { name: '', description: '' };
    return formValues;
  };

  const initialValues = buildFormikValues(dataflowValue);

  return (
    <Formik
      ref={form}
      enableReinitialize={true}
      initialValues={initialValues}
      validationSchema={dataflowCrudValidation}
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
      {({ isSubmitting, errors, touched, values }) => (
        <Form>
          <fieldset>
            <div className={`formField${!isEmpty(errors.dataflowName) && touched.dataflowName ? ' error' : ''}`}>
              <Field
                innerRef={inputRef}
                name="dataflowName"
                placeholder={resources.messages['createDataflowName']}
                type="text"
                value={values.name}
              />
            </div>
            <div
              className={`formField${
                !isEmpty(errors.dataflowDescription) && touched.dataflowDescription ? ' error' : ''
              }`}>
              <Field
                name="dataflowDescription"
                component="textarea"
                placeholder={resources.messages['createDataflowDescription']}
                value={values.description}
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
                label={isEditForm ? resources.messages['save'] : resources.messages['create']}
                disabled={isSubmitting}
                icon={isEditForm ? 'save' : 'add'}
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
