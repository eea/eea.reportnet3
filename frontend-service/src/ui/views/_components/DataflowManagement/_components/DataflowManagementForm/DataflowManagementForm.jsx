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

const DataflowManagementForm = ({
  data,
  getData,
  isEditForm,
  onCancel,
  onCreate,
  onEdit,
  onSearch,
  refresh,
  onResetData
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [hasErrors, setHasErrors] = useState(false);
  const [isNameDuplicated, setIsNameDuplicated] = useState(false);

  const form = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    if (!isNil(inputRef) && refresh) inputRef.current.focus();
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
    description: Yup.string().required().max(255, resources.messages['dataflowDescriptionValidationMax'])
  });

  return (
    <Formik
      ref={form}
      enableReinitialize={true}
      initialValues={data}
      validationSchema={dataflowCrudValidation}
      onSubmit={async (values, { setSubmitting }) => {
        setSubmitting(true);
        try {
          if (isEditForm) {
            await DataflowService.update(data.id, values.name, values.description, data.obligation.id);
            onEdit(values.name, values.description, data.obligation.id);
          } else {
            await DataflowService.create(values.name, values.description, data.obligation.id);
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
                    dataflowId: data.id,
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
                  getData({ ...data, name: event.target.value });
                  handleChange(event);
                  setIsNameDuplicated(false);
                  setHasErrors(false);
                }}
                type="text"
                value={data.name}
              />
              <ErrorMessage className="error" name="name" component="div" />
            </div>
            <div className={`formField${!isEmpty(errors.description) && touched.description ? ' error' : ''}`}>
              <Field
                name="description"
                component="textarea"
                onChange={event => getData({ ...data, description: event.target.value })}
                placeholder={resources.messages['createDataflowDescription']}
                value={data.description}
              />
              <ErrorMessage className="error" name="description" component="div" />
            </div>
            <div className={styles.search}>
              <Field
                className={styles.searchInput}
                name="associatedObligation"
                placeholder={resources.messages['associatedObligation']}
                type="text"
                readOnly={true}
                value={data.obligation.title}
              />
              <Button
                className={styles.searchButton}
                icon="search"
                label={resources.messages['search']}
                layout="simple"
                onClick={onSearch}
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
                className={`${styles.cancelButton} p-button-secondary p-button-animated-blink`}
                label={resources.messages['cancel']}
                icon="cancel"
                onClick={() => {
                  onCancel();
                  onResetData();
                }}
              />
            </div>
          </fieldset>
        </Form>
      )}
    </Formik>
  );
};

export { DataflowManagementForm };
