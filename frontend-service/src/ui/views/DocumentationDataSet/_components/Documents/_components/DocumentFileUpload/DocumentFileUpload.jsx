import React, { useContext, useRef } from 'react';

import * as Yup from 'yup';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import { isEmpty, isNull, isPlainObject, sortBy } from 'lodash';

import styles from './DocumentFileUpload.module.css';

import { config } from 'conf';

import { Button } from 'ui/views/_components/Button';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import { DocumentService } from 'core/services/Document';

const DocumentFileUpload = ({ dataflowId, onUpload, onGrowlAlert, isFormReset, setIsUploadDialogVisible }) => {
  const form = useRef(null);
  const resources = useContext(ResourcesContext);
  const initialValues = { description: '', lang: '', uploadFile: {} };
  const validationSchema = Yup.object().shape({
    description: Yup.string().required(),
    lang: Yup.string().required(),
    uploadFile: Yup.mixed()
      .test('fileEmpty', resources.messages['emptyFileValidationError'], value => {
        return !isPlainObject(value);
      })
      .test('fileSize', resources.messages['tooLargeFileValidationError'], value => {
        return value.size <= config.MAX_FILE_SIZE;
      })
  });

  if (!isNull(form.current) && !isFormReset) {
    form.current.resetForm();
  }

  return (
    <Formik
      ref={form}
      initialValues={initialValues}
      validationSchema={validationSchema}
      onSubmit={async (values, { setSubmitting }) => {
        setSubmitting(true);
        onGrowlAlert({
          severity: 'info',
          summary: resources.messages.documentUploadingGrowlUploadingSummary,
          detail: resources.messages.documentUploadingGrowlUploadingDetail,
          life: '5000'
        });

        const response = await DocumentService.uploadDocument(
          dataflowId,
          values.description,
          values.lang,
          values.uploadFile
        );
        onUpload();
        if (response === 200) {
          onGrowlAlert({
            severity: 'success',
            summary: resources.messages.documentUploadingGrowlSuccessSummary,
            detail: resources.messages.documentUploadingGrowlSuccessDetail,
            life: '5000'
          });
          setSubmitting(false);
          onUpload();
        } else {
          onGrowlAlert({
            severity: 'error',
            summary: resources.messages.documentUploadingGrowlErrorSummary,
            detail: resources.messages.documentUploadingGrowlErrorDetail,
            life: '5000'
          });
          setSubmitting(false);
        }
      }}>
      {({ isSubmitting, setFieldValue, errors, touched }) => (
        <Form>
          <fieldset>
            <div className={`formField${!isEmpty(errors.description) && touched.description ? ' error' : ''}`}>
              <Field name="description" type="text" placeholder={resources.messages.fileDescription} />
            </div>
            <div className={`formField${!isEmpty(errors.lang) && touched.lang ? ' error' : ''}`}>
              <Field name="lang" component="select">
                <option value="">{resources.messages.selectLang}</option>
                {sortBy(config.languages, ['name']).map(language => (
                  <option key={language.code} value={language.code}>
                    {language.name}
                  </option>
                ))}
              </Field>
            </div>
          </fieldset>
          <fieldset>
            <div className={`formField${!isEmpty(errors.uploadFile) && touched.uploadFile ? ' error' : ''}`}>
              <Field name="uploadFile">
                {() => (
                  <input
                    type="file"
                    name="uploadFile"
                    placeholder="file upload"
                    onChange={event => {
                      setFieldValue('uploadFile', event.currentTarget.files[0]);
                    }}
                  />
                )}
              </Field>
              <ErrorMessage name="uploadFile" component="div" />
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
                label={resources.messages['upload']}
                disabled={isSubmitting}
                icon="add"
                type={isSubmitting ? '' : 'submit'}
              />
              <Button
                className={`${styles.cancelButton} p-button-secondary`}
                label={resources.messages['cancel']}
                icon="cancel"
                onClick={() => setIsUploadDialogVisible(false)}
              />
            </div>
          </fieldset>
        </Form>
      )}
    </Formik>
  );
};

export { DocumentFileUpload };
