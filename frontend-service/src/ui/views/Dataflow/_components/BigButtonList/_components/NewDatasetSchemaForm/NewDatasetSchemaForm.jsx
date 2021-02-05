import React, { useContext, useEffect, useRef } from 'react';

import * as Yup from 'yup';
import { ErrorMessage, Field, Form, Formik } from 'formik';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './NewDatasetSchemaForm.module.css';

import { Button } from 'ui/views/_components/Button';

import { DataflowService } from 'core/services/Dataflow';

import { LoadingContext } from 'ui/views/_functions/Contexts/LoadingContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { MetadataUtils, TextUtils } from 'ui/views/_functions/Utils';

const NewDatasetSchemaForm = ({ dataflowId, datasetSchemaInfo, onCreate, onUpdateData, setNewDatasetDialog }) => {
  const { showLoading, hideLoading } = useContext(LoadingContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const form = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    if (!isNil(form.current)) form.current.resetForm();
  }, [form.current]);

  useEffect(() => {
    if (!isNil(inputRef.current)) inputRef.current.focus();
  }, [inputRef.current]);

  const initialValues = { datasetSchemaName: '' };
  const newDatasetValidationSchema = Yup.object().shape({
    datasetSchemaName: Yup.string()
      .required(' ')
      .test('', resources.messages['duplicateSchemaError'], value => {
        if (!isNil(value) && !isEmpty(datasetSchemaInfo)) {
          const schemas = [...datasetSchemaInfo];
          const isRepeat = schemas.filter(title => !TextUtils.areEquals(title.schemaName, value));
          return isRepeat.length === schemas.length;
        } else {
          return true;
        }
      })
  });

  return (
    <Formik
      ref={form}
      initialValues={initialValues}
      validationSchema={newDatasetValidationSchema}
      onSubmit={async (values, { setSubmitting }) => {
        setSubmitting(true);
        showLoading();
        try {
          const response = await DataflowService.newEmptyDatasetSchema(
            dataflowId,
            encodeURIComponent(values.datasetSchemaName)
          );
          if (response >= 200 && response <= 200) {
            onUpdateData();
            setSubmitting(false);
          } else {
            throw new Error('Schema creation error');
          }
          onCreate();
        } catch (error) {
          const metadata = await MetadataUtils.getMetadata({ dataflowId });
          const {
            dataflow: { name: dataflowName }
          } = metadata;

          if (error.response.data.message.includes('duplicated')) {
            notificationContext.add({
              type: 'DATASET_SCHEMA_CREATION_ERROR_DUPLICATED',
              content: { schemaName: values.datasetSchemaName }
            });
          } else {
            notificationContext.add({
              type: 'DATASET_SCHEMA_CREATION_ERROR',
              content: {
                dataflowId,
                dataflowName
              }
            });
            onCreate();
          }
        } finally {
          setSubmitting(false);
          hideLoading();
        }
      }}>
      {({ errors, isSubmitting, touched }) => (
        <Form>
          <fieldset>
            <div
              className={`formField${!isEmpty(errors.datasetSchemaName) && touched.datasetSchemaName ? ' error' : ''}`}>
              <Field
                id={'datasetSchemaName'}
                innerRef={inputRef}
                maxLength={250}
                name="datasetSchemaName"
                placeholder={resources.messages['createDatasetSchemaName']}
                type="text"
              />
              <label htmlFor="datasetSchemaName" className="srOnly">
                {resources.messages['createDatasetSchemaName']}
              </label>
              <ErrorMessage className="error" name="datasetSchemaName" component="div" />
            </div>
          </fieldset>
          <fieldset>
            <div className={`${styles.buttonWrap} ui-dialog-buttonpane p-clearfix`}>
              <Button
                className="p-button-primary"
                disabled={isSubmitting}
                label={resources.messages['create']}
                icon="add"
                type="submit"
              />
              <Button
                className={`${styles.cancelButton} p-button-secondary button-right-aligned`}
                icon="cancel"
                label={resources.messages['cancel']}
                onClick={() => setNewDatasetDialog(false)}
              />
            </div>
          </fieldset>
        </Form>
      )}
    </Formik>
  );
};

export { NewDatasetSchemaForm };
