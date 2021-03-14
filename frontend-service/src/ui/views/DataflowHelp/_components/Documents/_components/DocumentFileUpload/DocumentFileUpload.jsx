import React, { Fragment, useContext, useEffect, useRef, useState } from 'react';

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
  setIsUploadDialogVisible,
  setFileUpdatingId,
  setIsUpdating
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [isUploading, setIsUploading] = useState(false);

  const form = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    if (!isNil(form.current)) {
      form.current.resetForm();
      document.querySelector('.uploadFile').value = '';
    }
  }, [form.current]);

  useEffect(() => {
    if (isUploadDialogVisible) inputRef.current.focus();
  }, [isUploadDialogVisible]);

  const validationSchema = Yup.object().shape({
    description: Yup.string().required(' ').max(255, resources.messages['documentDescriptionValidationMax']),
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

  const IsPublicCheckbox = ({ checked, field, type }) => (
    <Fragment>
      <input id="isPublic" {...field} type={type} checked={checked} />
      <label htmlFor="isPublic" style={{ display: 'block' }}>
        {resources.messages['documentUploadCheckboxIsPublic']}
      </label>
    </Fragment>
  );

  return (
    <Formik
      ref={form}
      enableReinitialize={true}
      initialValues={documentInitialValues}
      validationSchema={validationSchema}
      onSubmit={async (values, { setSubmitting }) => {
        if (!isEqual(documentInitialValues, values)) {
          setIsUploading(true);
          setSubmitting(true);
          setFileUpdatingId(values.id);
          notificationContext.add({
            type: 'DOCUMENT_UPLOADING_INIT_INFO',
            content: {}
          });
          try {
            if (isEditForm) {
              setIsUpdating(true);
              await DocumentService.editDocument(
                dataflowId,
                values.description,
                values.lang,
                values.uploadFile,
                values.isPublic,
                values.id
              );
              onUpload();
            } else {
              await DocumentService.uploadDocument(
                dataflowId,
                values.description,
                values.lang,
                values.uploadFile,
                values.isPublic
              );
              onUpload();
            }
          } catch (error) {
            if (isEditForm) {
              notificationContext.add({
                type: 'DOCUMENT_EDITING_ERROR',
                content: {}
              });
              setIsUpdating(false);
            } else {
              notificationContext.add({
                type: 'DOCUMENT_UPLOADING_ERROR',
                content: {}
              });
            }
            onUpload();
            setFileUpdatingId('');
          } finally {
            setIsUploading(false);
          }
        } else {
          setSubmitting(false);
        }
      }}>
      {({ errors, isSubmitting, setFieldValue, touched, values }) => (
        <Form>
          <fieldset>
            <div className={`formField${!isEmpty(errors.description) && touched.description ? ' error' : ''}`}>
              <Field
                id={'descriptionDocumentFileUpload'}
                innerRef={inputRef}
                maxLength={255}
                name="description"
                placeholder={resources.messages['fileDescription']}
                type="text"
                value={values.description}
              />
              <label htmlFor="descriptionDocumentFileUpload" className="srOnly">
                {resources.messages['description']}
              </label>
              <ErrorMessage className="error" name="description" component="div" />
            </div>
            <div className={`formField${!isEmpty(errors.lang) && touched.lang ? ' error' : ''}`}>
              <Field id={'selectLanguage'} name="lang" component="select" value={values.lang}>
                <option value="">{resources.messages['selectLang']}</option>
                {sortBy(config.languages, ['name']).map(language => (
                  <option key={language.code} value={language.code}>
                    {language.name}
                  </option>
                ))}
              </Field>
              <label htmlFor="selectLanguage" className="srOnly">
                {resources.messages['selectLang']}
              </label>
            </div>
          </fieldset>
          <fieldset>
            <div className={`formField${!isEmpty(errors.uploadFile) && touched.uploadFile ? ' error' : ''}`}>
              <Field name="uploadFile">
                {() => (
                  <span>
                    <input
                      className="uploadFile"
                      id={'uploadDocument'}
                      name="uploadFile"
                      onChange={event => {
                        setFieldValue('uploadFile', event.currentTarget.files[0]);
                      }}
                      placeholder="file upload"
                      type="file"
                    />
                    <label htmlFor="uploadDocument" className="srOnly">
                      {resources.messages['uploadDocument']}
                    </label>
                  </span>
                )}
              </Field>
              <ErrorMessage name="uploadFile" component="div" />
            </div>
          </fieldset>
          <fieldset>
            <div className={styles.checkboxIsPublic}>
              <Field checked={values.isPublic} component={IsPublicCheckbox} name="isPublic" type="checkbox" />
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
                disabled={isSubmitting || isUploading}
                icon={!isUploading ? (isEditForm ? 'check' : 'add') : 'spinnerAnimate'}
                label={isEditForm ? resources.messages['save'] : resources.messages['upload']}
                type={isSubmitting ? '' : 'submit'}
              />
              <Button
                className={`${styles.cancelButton} p-button-secondary button-right-aligned`}
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
