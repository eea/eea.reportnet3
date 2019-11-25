import React, { useContext, useRef } from 'react';

import * as Yup from 'yup';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import { isEmpty, isNull, isPlainObject, sortBy, isUndefined } from 'lodash';

import styles from './DocumentFileUpload.module.scss';

import { config } from 'conf';

import { Button } from 'ui/views/_components/Button';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import { DocumentService } from 'core/services/Document';

const DocumentFileUpload = ({
  dataflowId,
  documentInitialValues,
  onUpload,
  onGrowlAlert,
  isFormReset,
  setIsUploadDialogVisible,
  isEditForm = false
}) => {
  const form = useRef(null);
  const resources = useContext(ResourcesContext);
  const validationSchema = Yup.object().shape({
    description: Yup.string().required(),
    lang: Yup.string().required(),
    uploadFile: isEditForm
      ? Yup.mixed()
          .test('checkSizeWhenNotEmpty', resources.messages['tooLargeFileValidationError'], value => {
            if (isUndefined(value) || isPlainObject(value) || isNull(value)) {
              return true;
            } else {
              return value.size <= config.MAX_FILE_SIZE;
            }
          })
          .nullable()
      : Yup.mixed()
          .test('fileEmpty', resources.messages['emptyFileValidationError'], value => {
            return !isPlainObject(value);
          })
          .test('fileSize', resources.messages['tooLargeFileValidationError'], value => {
            return value.size <= config.MAX_FILE_SIZE;
          })
  });

  if (!isNull(form.current) && !isFormReset) {
    form.current.resetForm();
    document.querySelector('.uploadFile').value = '';
  }

  const Checkbox = ({ field, type, checked }) => {
    return (
      <>
        <input id="isPublic" {...field} type={type} checked={checked} />
        <label htmlFor="isPublic" style={{ display: 'block' }}>
          {resources.messages['documentUploadCheckboxIsPublic']}
        </label>
      </>
    );
  };

  const buildInitialValue = documentInitialValues => {
    let initiaValues = { description: '', lang: '', uploadFile: {}, isPublic: false };
    if (isEditForm) {
      const langField = {
        lang: config.languages
          .filter(language => language.name == documentInitialValues.language)
          .map(country => country.code)
      };
      initiaValues = Object.assign({}, documentInitialValues, langField);
      initiaValues.uploadFile = {};
    }
    return initiaValues;
  };

  const initiaValueslWithLangField = buildInitialValue(documentInitialValues);

  return (
    <Formik
      ref={form}
      enableReinitialize={true}
      initialValues={initiaValueslWithLangField}
      validationSchema={validationSchema}
      onSubmit={async (values, { setSubmitting }) => {
        setSubmitting(true);
        onGrowlAlert({
          severity: 'info',
          summary: resources.messages['documentUploadingGrowlUploadingSummary'],
          detail: resources.messages['documentUploadingGrowlUploadingDetail'],
          life: '5000'
        });

        const response = isEditForm
          ? await DocumentService.editDocument(
              dataflowId,
              values.description,
              values.lang,
              values.uploadFile,
              values.isPublic
            )
          : await DocumentService.uploadDocument(
              dataflowId,
              values.description,
              values.lang,
              values.uploadFile,
              values.isPublic
            );

        onUpload();
        if (response === 200) {
          onGrowlAlert({
            severity: 'success',
            summary: resources.messages['documentUploadingGrowlSuccessSummary'],
            detail: resources.messages['documentUploadingGrowlSuccessDetail'],
            life: '5000'
          });
          setSubmitting(false);
          onUpload();
        } else {
          onGrowlAlert({
            severity: 'error',
            summary: resources.messages['documentUploadingGrowlErrorSummary'],
            detail: resources.messages['documentUploadingGrowlErrorDetail'],
            life: '5000'
          });
          setSubmitting(false);
        }
      }}>
      {({ isSubmitting, setFieldValue, errors, touched, values }) => (
        <Form>
          <fieldset>
            <div className={`formField${!isEmpty(errors.description) && touched.description ? ' error' : ''}`}>
              <Field
                name="description"
                type="text"
                placeholder={resources.messages.fileDescription}
                value={values.description}
              />
            </div>
            <div className={`formField${!isEmpty(errors.lang) && touched.lang ? ' error' : ''}`}>
              <Field name="lang" component="select" value={values.lang}>
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
                    className="uploadFile"
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
            <div className={styles.checkboxIsPublick}>
              <Field name="isPublic" type="checkbox" checked={values.isPublic} component={Checkbox} />
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
                label={isEditForm ? resources.messages['save'] : resources.messages['upload']}
                disabled={isSubmitting}
                icon={isEditForm ? 'save' : 'add'}
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
