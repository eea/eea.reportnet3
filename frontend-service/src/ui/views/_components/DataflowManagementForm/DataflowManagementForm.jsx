import React, { useContext, useEffect, useRef } from 'react';

import * as Yup from 'yup';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import { isEmpty, isNull, isUndefined } from 'lodash';

import styles from './DataflowManagementForm.module.css';

import { Button } from 'ui/views/_components/Button';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

import { DataflowService } from 'core/services/Dataflow';

const DataflowManagementForm = ({
  dataflowId,
  dataflowValues,
  isDialogVisible,
  isEditForm,
  isFormReset,
  onCreate,
  onCancel,
  onEdit,
  selectedDataflow
}) => {
  const notificationContext = useContext(NotificationContext);
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
    name: isEditForm
      ? Yup.string()
          .required(' ')
          .test('isEditFormDuplicated', resources.messages['duplicatedDataflow'], value => {
            if (!isUndefined(value) && !isEmpty(dataflowValues)) {
              const repeatedResults = Object.keys(dataflowValues)
                .filter(key => dataflowValues[key].id != dataflowId)
                .map(key => dataflowValues[key].name)
                .filter(item => typeof item === 'string')
                .some(item => item.toLowerCase() === value.toLowerCase());
              return !repeatedResults;
            }
          })
      : Yup.string()
          .required(' ')
          .test('isNewFormDuplicated', resources.messages['duplicatedDataflow'], value => {
            if (!isUndefined(value) && !isEmpty(dataflowValues)) {
              const isRepeat = Object.keys(dataflowValues).some(
                key => dataflowValues[key].name.toLowerCase() === value.toLowerCase()
              );
              return !isRepeat;
            } else {
              return true;
            }
          }),
    description: Yup.string().required()
  });

  if (!isNull(form.current) && !isFormReset) {
    form.current.resetForm();
  }

  const buildFormikValues = selectedValues => {
    const formValues = isEditForm ? selectedValues : { name: '', description: '' };
    return formValues;
  };

  const initialValues = buildFormikValues(selectedDataflow);

  return (
    <Formik
      ref={form}
      enableReinitialize={true}
      initialValues={initialValues}
      validationSchema={dataflowCrudValidation}
      onSubmit={async (values, { setSubmitting }) => {
        setSubmitting(true);
        try {
          if (isEditForm) {
            await DataflowService.update(dataflowId, values.name, values.description);
            onEdit(dataflowId, values.name, values.description);
          } else {
            await DataflowService.create(values.name, values.description);
            onCreate();
          }
        } catch (error) {
          const notification = isEditForm
            ? {
                type: 'DATAFLOW_UPDATING_ERROR',
                content: {
                  dataflowId,
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
        } finally {
          setSubmitting(false);
        }
      }}>
      {({ isSubmitting, errors, touched, values }) => (
        <Form>
          <fieldset>
            <div className={`formField${!isEmpty(errors.name) && touched.name ? ' error' : ''}`}>
              <Field
                innerRef={inputRef}
                name="name"
                placeholder={resources.messages['createDataflowName']}
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

export { DataflowManagementForm };
