import React, { useContext, useRef } from 'react';

import * as Yup from 'yup';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import { isEmpty, isNull, isUndefined } from 'lodash';

import styles from './NewDatasetSchemaForm.module.css';

import { Button } from 'ui/views/_components/Button';

import { DataflowService } from 'core/services/Dataflow';

import { LoadingContext } from 'ui/views/_functions/Contexts/LoadingContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { MetadataUtils } from 'ui/views/_functions/Utils/MetadataUtils';

const NewDatasetSchemaForm = ({
  dataflowId,
  datasetSchemaInfo,
  isFormReset,
  onCreate,
  onUpdateData,
  setNewDatasetDialog
}) => {
  const { showLoading, hideLoading } = useContext(LoadingContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const form = useRef(null);

  const initialValues = { datasetSchemaName: '' };
  const newDatasetValidationSchema = Yup.object().shape({
    datasetSchemaName: Yup.string()
      .required(' ')
      .test('', resources.messages['duplicateSchemaError'], value => {
        if (!isUndefined(value) && !isEmpty(datasetSchemaInfo)) {
          const schemas = [...datasetSchemaInfo];
          const isRepeat = schemas.filter(title => title.schemaName.toLowerCase() !== value.toLowerCase());
          return isRepeat.length === schemas.length;
        } else {
          return true;
        }
      })
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
        } catch (error) {
          const metadata = await MetadataUtils.getMetadata({ dataflowId });
          const {
            dataflow: { name: dataflowName }
          } = metadata;
          notificationContext.add({
            type: 'DATASET_SCHEMA_CREATION_ERROR',
            content: {
              dataflowId,
              dataflowName
            }
          });
        } finally {
          onCreate();
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
                id="dataSchemaInput"
                name="datasetSchemaName"
                placeholder={resources.messages['createDatasetSchemaName']}
                type="text"
              />
              <ErrorMessage className="error" name="datasetSchemaName" component="div" />
            </div>
          </fieldset>
          <fieldset>
            <div className={`${styles.buttonWrap} ui-dialog-buttonpane p-clearfix`}>
              <Button
                className={styles.primaryButton}
                disabled={isSubmitting}
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

export { NewDatasetSchemaForm };
