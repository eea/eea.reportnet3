import React from 'react';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';

const DocumentFileUpload = () => {
  return (
    <Formik
      initialValues={{ title: '', description: '', lang: '', uploadFile: null }}
      validationSchema={validationSchema}
      onSubmit={(values, { setSubmitting }) => {
        // onLogin(values.user, values.password);
        console.log('file name: ', values.uploadFile);
        setSubmitting(false);
      }}>
      {({ isSubmitting, setFieldValue }) => (
        <Form>
          <fieldset>
            <Field name="title" type="text" placeholder="file title" />
            <ErrorMessage name="title" component="div" />
            <Field name="description" type="text" placeholder="file description" />
            <ErrorMessage name="description" component="div" />
            <Field name="lang" component="select" placeholder="select">
              <option>select lang</option>
              <option value="eus">eus</option>
              <option value="en">en</option>
              <option value="es">es</option>
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
          </fieldset>
          <fieldset>
            <button type="submit" disabled={isSubmitting}>
              Upload
            </button>
            <button type="reset">Reset</button>
          </fieldset>
        </Form>
      )}
    </Formik>
  );
};

const validationSchema = Yup.object().shape({
  title: Yup.string().required('please enter a title'),
  description: Yup.string().required('please enter a description'),
  lang: Yup.string().required('language must be selected')
});

export { DocumentFileUpload };
