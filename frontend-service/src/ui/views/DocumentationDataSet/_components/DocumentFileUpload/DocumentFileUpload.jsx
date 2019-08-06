import React, { useContext } from 'react';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import { isPlainObject } from 'lodash';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { DocumentService } from 'core/services/Document';

import { config } from 'assets/conf';

const DocumentFileUpload = ({ dataFlowId, onUpload }) => {
  const resources = useContext(ResourcesContext);
  const initialValues = { title: '', description: '', lang: '', uploadFile: {} };
  const validationSchema = Yup.object().shape({
    title: Yup.string().required(resources.messages.emptyTitleValidationError),
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
    <Formik
      initialValues={initialValues}
      validationSchema={validationSchema}
      onSubmit={async (values, { setSubmitting }) => {
        const response = await DocumentService.uploadDocument(
          dataFlowId,
          values.title,
          values.description,
          values.lang,
          values.uploadFile
        );
        if (response.status === 200) {
          setSubmitting(false);
          onUpload();
        }
      }}>
      {({ isSubmitting, setFieldValue }) => (
        <Form>
          <fieldset>
            <Field name="title" type="text" placeholder={resources.messages.fileTitle} />
            <ErrorMessage name="title" component="div" />
            <Field name="description" type="text" placeholder={resources.messages.fileDescription} />
            <ErrorMessage name="description" component="div" />
            <Field name="lang" component="select" placeholder={resources.messages.select}>
              <option>{resources.messages.selectLang}</option>
              {config.languages.map(language => (
                <option key={`lang-${language.code}`} value={language.code}>
                  {language.code}
                </option>
              ))}
            </Field>
            <ErrorMessage name="lang" component="div" />
          </fieldset>
          <fieldset>
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
          </fieldset>
          <fieldset>
            <button type="submit" disabled={isSubmitting}>
              {resources.messages.upload}
            </button>
            <button type="reset">{resources.messages.reset}</button>
          </fieldset>
        </Form>
      )}
    </Formik>
  );
};

export { DocumentFileUpload };
