import React, { useContext } from 'react';

import * as Yup from 'yup';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import { isPlainObject, isEmpty } from 'lodash';

import styles from './DocumentFileUpload.module.css';

import { config } from 'conf';

import { Button } from 'ui/views/_components/Button';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import { DocumentService } from 'core/services/Document';

const DocumentFileUpload = ({ dataFlowId, onUpload, onGrowlAlert }) => {
  const resources = useContext(ResourcesContext);
  const initialValues = { /* title: '', */ description: '', lang: '', uploadFile: {} };
  const validationSchema = Yup.object().shape({
    // title: Yup.string().required(resources.messages.emptyTitleValidationError),
    description: Yup.string().required(resources.messages.emptyDescriptionValidationError),
    lang: Yup.string().required(resources.messages.emptyLanguageValidationError),
    uploadFile: Yup.mixed()
      .test('fileEmpty', resources.messages.emptyFileValidationError, value => {
        return !isPlainObject(value);
      })
      .test('fileSize', resources.messages.tooLargeFileValidationError, value => {
        return value.size <= config.MAX_FILE_SIZE;
      })
    // .test('fileType', 'Unsupported File Format', value => ['application/pdf'].includes(value.type))
  });

  return (
    <>
      <Formik
        initialValues={initialValues}
        validationSchema={validationSchema}
        onSubmit={async (values, { setSubmitting }) => {
          setSubmitting(true);
          onGrowlAlert({
            severity: 'info',
            // summary: resources.messages['datasetLoadingTitle'],
            summary: resources.messages.documentUploadingGrowlUploadingSummary,
            detail: resources.messages.documentUploadingGrowlUploadingDetail,
            life: '5000'
          });

          const response = await DocumentService.uploadDocument(
            dataFlowId,
            // values.title,
            values.description,
            values.lang,
            values.uploadFile
          );
          onUpload();
          if (response === 200) {
            onGrowlAlert({
              severity: 'success',
              // summary: resources.messages['datasetLoadingTitle'],
              summary: resources.messages.documentUploadingGrowlSuccessSummary,
              detail: resources.messages.documentUploadingGrowlSuccessDetail,
              life: '5000'
            });
            setSubmitting(false);
            onUpload();
          } else {
            onGrowlAlert({
              severity: 'error',
              // summary: resources.messages['datasetLoadingTitle'],
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
              {/* <div className={`formField${!isEmpty(errors.title) && touched.title ? ' error' : ''}`}>
              <Field name="title" type="text" placeholder={resources.messages.fileTitle} />
              <ErrorMessage className="error" name="title" component="div" />
            </div> */}
              <div className={`formField${!isEmpty(errors.description) && touched.description ? ' error' : ''}`}>
                <Field name="description" type="text" placeholder={resources.messages.fileDescription} />
                <ErrorMessage className="error" name="description" component="div" />
              </div>
              <div className={`formField${!isEmpty(errors.lang) && touched.lang ? ' error' : ''}`}>
                <Field name="lang" component="select">
                  <option value="">{resources.messages.selectLang}</option>
                  {config.languages.map(language => (
                    <option key={language.code} value={language.code}>
                      {language.name}
                    </option>
                  ))}
                </Field>
                <ErrorMessage className="error" name="lang" component="div" />
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
              <div className={styles.btnWrapper}>
                <Button
                  type="submit"
                  className={styles.primaryBtn}
                  disabled={isSubmitting}
                  label={resources.messages.upload}
                  layout="simple"
                />
                <Button type="reset" className={styles.defaultBtn} label={resources.messages.reset} layout="simple" />
              </div>
            </fieldset>
          </Form>
        )}
      </Formik>
    </>
  );
};

export { DocumentFileUpload };
