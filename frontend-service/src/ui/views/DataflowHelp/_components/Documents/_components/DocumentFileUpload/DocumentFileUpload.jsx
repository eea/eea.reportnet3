import React, { useContext, useEffect, useRef } from 'react';

import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import isEmpty from 'lodash/isEmpty';
import isEqual from 'lodash/isEqual';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isPlainObject from 'lodash/isPlainObject';
import isUndefined from 'lodash/isUndefined';
import sortBy from 'lodash/sortBy';

import styles from './DocumentFileUpload.module.scss';

import { config } from 'conf';

import { Button } from 'ui/views/_components/Button';

import { DocumentService } from 'core/services/Document';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const DocumentFileUpload = ({
  dataflowId,
  documentInitialValues,
  isEditForm = false,
  isUploadDialogVisible,
  onUpload,
  setIsUploadDialogVisible
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const form = useRef(null);
  const inputRef = useRef();

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
          .test('fileEmpty', ' ', value => {
            return !isPlainObject(value);
          })
          .test('fileSize', resources.messages['tooLargeFileValidationError'], value => {
            return value.size <= config.MAX_FILE_SIZE;
          })
  });

  useEffect(() => {
    if (!isNil(form.current)) {
      form.current.resetForm();
      document.querySelector('.uploadFile').value = '';
    }
  }, [form.current]);

  useEffect(() => {
    if (isUploadDialogVisible) inputRef.current.focus();
  }, [isUploadDialogVisible]);

  const buildInitialValue = documentInitialValues => {
    let initialValues = { description: '', lang: '', uploadFile: {}, isPublic: false };
    if (isEditForm) {
      const langField = {
        lang: config.languages
          .filter(language => language.name == documentInitialValues.language)
          .map(country => country.code)
      };
      initialValues = Object.assign({}, documentInitialValues, langField);
      initialValues.uploadFile = {};
    }
    return initialValues;
  };

  const initialValuesWithLangField = buildInitialValue(documentInitialValues);

  const IsPublicCheckbox = ({ field, type, checked }) => {
    return (
      <>
        <input id="isPublic" {...field} type={type} checked={checked} />
        <label htmlFor="isPublic" style={{ display: 'block' }}>
          {resources.messages['documentUploadCheckboxIsPublic']}
        </label>
      </>
    );
  };

  return (
    <Formik
      ref={form}
      enableReinitialize={true}
      initialValues={initialValuesWithLangField}
      validationSchema={validationSchema}
      onSubmit={async (values, { setSubmitting }) => {
        if (!isEqual(initialValuesWithLangField, values)) {
          setSubmitting(true);
          notificationContext.add({
            type: 'DOCUMENT_UPLOADING_INIT_INFO',
            content: {}
          });
          try {
            if (isEditForm) {
              onUpload();
              await DocumentService.editDocument(
                dataflowId,
                values.description,
                values.lang,
                values.uploadFile,
                values.isPublic,
                values.id
              );
            } else {
              onUpload();
              await DocumentService.uploadDocument(
                dataflowId,
                values.description,
                values.lang,
                values.uploadFile,
                values.isPublic
              );
            }
          } catch (error) {
            if (isEditForm) {
              notificationContext.add({
                type: 'DOCUMENT_EDITING_ERROR',
                content: {}
              });
            } else {
              notificationContext.add({
                type: 'DOCUMENT_UPLOADING_ERROR',
                content: {}
              });
            }
          } finally {
            setSubmitting(false);
          }
        } else {
          setIsUploadDialogVisible(false);
        }
      }}>
      {({ isSubmitting, setFieldValue, errors, touched, values }) => (
        <Form>
          <fieldset>
            <div className={`formField${!isEmpty(errors.description) && touched.description ? ' error' : ''}`}>
              <Field
                innerRef={inputRef}
                name="description"
                placeholder={resources.messages['fileDescription']}
                type="text"
                value={values.description}
              />
            </div>
            <div className={`formField${!isEmpty(errors.lang) && touched.lang ? ' error' : ''}`}>
              <Field name="lang" component="select" value={values.lang}>
                <option value="">{resources.messages['selectLang']}</option>
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
                    name="uploadFile"
                    onChange={event => {
                      setFieldValue('uploadFile', event.currentTarget.files[0]);
                    }}
                    placeholder="file upload"
                    type="file"
                  />
                )}
              </Field>
              <ErrorMessage name="uploadFile" component="div" />
            </div>
          </fieldset>
          <fieldset>
            <div className={styles.checkboxIsPublic}>
              <Field name="isPublic" type="checkbox" checked={values.isPublic} component={IsPublicCheckbox} />
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
                disabled={isSubmitting}
                icon={isEditForm ? 'save' : 'add'}
                label={isEditForm ? resources.messages['save'] : resources.messages['upload']}
                type={isSubmitting ? '' : 'submit'}
              />
              <Button
                className={`${styles.cancelButton} p-button-secondary`}
                icon="cancel"
                label={resources.messages['cancel']}
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
