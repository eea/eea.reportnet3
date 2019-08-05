import React from 'react';
import { Formik } from 'formik';
import * as Yup from 'yup';

const DocumentFileUpload = () => {
  return (
    <Formik
      initialValues={{ title: '', description: '', lang: '' }}
      validationSchema={validationSchema}
      onSubmit={(values, { setSubmitting }) => {
        // onLogin(values.user, values.password);
        setSubmitting(false);
      }}>
      {({ isSubmitting }) => (
        <form>
          <fieldset>
            <input type="text" name="title" placeholder="file title" />
            <input type="text" name="description" placeholder="file description" />
            <select name="lang">
              <option>select lang</option>
              <option value="eus">eus</option>
              <option value="en">en</option>
              <option value="es">es</option>
            </select>
          </fieldset>
          <fieldset>
            <button type="submit" disabled={isSubmitting}>
              Upload
            </button>
          </fieldset>
        </form>
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
