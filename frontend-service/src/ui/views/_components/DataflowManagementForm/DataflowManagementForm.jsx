import React, { useContext, useEffect, useRef, useState } from 'react';

import * as Yup from 'yup';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './DataflowManagementForm.module.css';

import DataflowConf from 'conf/dataflow.config.json';

import { Button } from 'ui/views/_components/Button';

import { DataflowService } from 'core/services/Dataflow';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const DataflowManagementForm = React.memo(({ dataflowData, isEditForm, onCancel, onCreate, onEdit, refresh }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [hasErrors, setHasErrors] = useState(false);
  const [isNameDuplicated, setIsNameDuplicated] = useState(false);

  const form = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    if (!isNil(inputRef) && refresh) {
      inputRef.current.focus();
    }
  }, [hasErrors, inputRef.current, refresh]);

  useEffect(() => {
    if (!isNil(form.current) && refresh) {
      form.current.resetForm();
      setIsNameDuplicated(false);
      setHasErrors(false);
    }
  }, [refresh, form.current]);

  const dataflowCrudValidation = Yup.object().shape({
    name: Yup.string().required(' '),
    description: Yup.string()
      .required()
      .max(255, resources.messages['dataflowDescriptionValidationMax'])
  });

  return (
    <Formik
      ref={form}
      enableReinitialize={true}
      initialValues={isEditForm ? dataflowData : { name: '', description: '' }}
      validationSchema={dataflowCrudValidation}
      onSubmit={async (values, { setSubmitting }) => {
        setSubmitting(true);
        try {
          if (isEditForm) {
            await DataflowService.update(dataflowData.id, values.name, values.description);
            onEdit(values.name, values.description);
          } else {
            await DataflowService.create(values.name, values.description);
            onCreate();
          }
        } catch (error) {
          setHasErrors(true);
          if (error.response.data == DataflowConf.errorTypes['dataflowExists']) {
            setIsNameDuplicated(true);
            notificationContext.add({
              type: 'DATAFLOW_NAME_EXISTS'
            });
          } else {
            const notification = isEditForm
              ? {
                  type: 'DATAFLOW_UPDATING_ERROR',
                  content: {
                    dataflowId: dataflowData.id,
                    dataflowName: values.name
                  }
                }
              : {
                  type: 'DATAFLOW_CREATION_ERROR',
                  content: {
                    dataflowName: values.name
                  }
                };
            notificationContext.add(notification);
          }
        } finally {
          setSubmitting(false);
        }
      }}>
      {({ errors, handleChange, isSubmitting, touched, values }) => (
        <Form>
          <fieldset>
            <div className={`formField${(!isEmpty(errors.name) && touched.name) || isNameDuplicated ? ' error' : ''}`}>
              <Field
                innerRef={inputRef}
                name="name"
                placeholder={resources.messages['createDataflowName']}
                onChange={event => {
                  handleChange(event);
                  setIsNameDuplicated(false);
                  setHasErrors(false);
                }}
                type="text"
                value={values.name}
              />
              <ErrorMessage className="error" name="name" component="div" />
            </div>
            <div className={`formField${!isEmpty(errors.description) && touched.description ? ' error' : ''}`}>
              <Field
                name="description"
                component="textarea"
                placeholder={resources.messages['createDataflowDescription']}
                value={values.description}
              />
              <ErrorMessage className="error" name="description" component="div" />
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
                className={`${
                  !isEmpty(touched)
                    ? isEmpty(errors)
                      ? styles.primaryButton
                      : styles.disabledButton
                    : styles.disabledButton
                } p-button-primary p-button-animated-blink`}
                label={isEditForm ? resources.messages['save'] : resources.messages['create']}
                disabled={isSubmitting}
                icon={isEditForm ? 'save' : 'add'}
                type={isSubmitting ? '' : 'submit'}
              />
              <Button
                className={`${styles.cancelButton} ${
                  !isEditForm ? 'p-button-secondary' : 'p-button-danger'
                }  p-button-animated-blink`}
                label={resources.messages['cancel']}
                icon="cancel"
                onClick={onCancel}
              />
            </div>
          </fieldset>
        </Form>
      )}
    </Formik>
  );
});

export { DataflowManagementForm };
