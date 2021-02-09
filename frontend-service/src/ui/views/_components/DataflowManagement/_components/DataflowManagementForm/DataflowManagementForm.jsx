import React, { forwardRef, useContext, useEffect, useImperativeHandle, useRef, useState } from 'react';

import * as Yup from 'yup';
import { ErrorMessage, Field, Form, Formik } from 'formik';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './DataflowManagementForm.module.scss';

import DataflowConf from 'conf/dataflow.config.json';

import { Button } from 'ui/views/_components/Button';

import { DataflowService } from 'core/services/Dataflow';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const DataflowManagementForm = forwardRef(
  ({ data, dataflowId, getData, isEditForm, onCreate, onEdit, onSearch, onSubmit, refresh }, ref) => {
    const notificationContext = useContext(NotificationContext);
    const resources = useContext(ResourcesContext);

    const [hasErrors, setHasErrors] = useState(false);
    const [isNameDuplicated, setIsNameDuplicated] = useState(false);
    const [isObligationEmpty, setIsObligationEmpty] = useState(false);

    const form = useRef(null);
    const inputRef = useRef(null);

    useEffect(() => {
      if (!isNil(inputRef) && refresh) inputRef.current.focus();
    }, [hasErrors, inputRef.current, refresh]);

    useEffect(() => {
      if (!isNil(form.current) && refresh) {
        form.current.resetForm();
        setHasErrors(false);
        setIsNameDuplicated(false);
        setIsObligationEmpty(false);
      }
    }, [refresh, form.current]);

    useImperativeHandle(ref, () => ({
      handleSubmit: () => form.current.handleSubmit()
    }));

    const dataflowCrudValidation = Yup.object().shape({
      name: Yup.string().required(' ').max(255, resources.messages['dataflowNameValidationMax']),
      description: Yup.string().required(' ').max(255, resources.messages['dataflowDescriptionValidationMax']),
      obligation: Yup.object({ title: Yup.string().required(' ') })
    });

    return (
      <Formik
        ref={form}
        enableReinitialize={true}
        initialValues={data}
        validationSchema={dataflowCrudValidation}
        onSubmit={async values => {
          onSubmit(true);
          try {
            if (isEditForm) {
              await DataflowService.update(
                dataflowId,
                values.name,
                values.description,
                data.obligation.id,
                data.isReleasable
              );
              onEdit(values.name, values.description, data.obligation.id);
            } else {
              await DataflowService.create(values.name, values.description, data.obligation.id);
              onCreate();
            }
          } catch (error) {
            setHasErrors(true);
            if (error.response.data === DataflowConf.errorTypes['dataflowExists']) {
              setIsNameDuplicated(true);
              notificationContext.add({ type: 'DATAFLOW_NAME_EXISTS' });
            } else {
              const notification = isEditForm
                ? { type: 'DATAFLOW_UPDATING_ERROR', content: { dataflowId: data.id, dataflowName: values.name } }
                : { type: 'DATAFLOW_CREATION_ERROR', content: { dataflowName: values.name } };

              notificationContext.add(notification);
            }
          } finally {
            onSubmit(false);
          }
        }}>
        {({ errors, handleChange, touched }) => (
          <Form>
            <fieldset>
              <div
                className={`formField${(!isEmpty(errors.name) && touched.name) || isNameDuplicated ? ' error' : ''}`}>
                <Field
                  autoComplete="off"
                  id="dataflowName"
                  innerRef={inputRef}
                  maxLength={255}
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
                <label htmlFor="dataflowName" className="srOnly">
                  {resources.messages['createDataflowName']}
                </label>
                <ErrorMessage className="error" name="name" component="div" />
              </div>

              <div className={`formField${!isEmpty(errors.description) && touched.description ? ' error' : ''}`}>
                <Field
                  autoComplete="off"
                  id="dataflowDescription"
                  name="description"
                  component="textarea"
                  rows="10"
                  onChange={event => getData({ ...data, description: event.target.value })}
                  placeholder={resources.messages['createDataflowDescription']}
                  value={data.description}
                />
                <label htmlFor="dataflowDescription" className="srOnly">
                  {resources.messages['createDataflowDescription']}
                </label>
                <ErrorMessage className="error" name="description" component="div" />
              </div>

              <div className={`${styles.search}`}>
                <Button icon="search" label={resources.messages['searchObligations']} onMouseDown={onSearch} />
                <Field
                  id="searchObligation"
                  className={`${styles.searchInput} ${
                    (!isEmpty(errors.obligation) && !isEmpty(touched.obligation) && touched.obligation.title) ||
                    isObligationEmpty
                      ? styles.searchErrors
                      : ''
                  }`}
                  name="obligation.title"
                  placeholder={resources.messages['associatedObligation']}
                  readOnly={true}
                  type="text"
                  value={data.obligation.title}
                />
                <label htmlFor="searchObligation" className="srOnly">
                  {resources.messages['searchObligations']}
                </label>
                <ErrorMessage className="error" name="obligation.title" component="div" />
              </div>
            </fieldset>
          </Form>
        )}
      </Formik>
    );
  }
);

export { DataflowManagementForm };
