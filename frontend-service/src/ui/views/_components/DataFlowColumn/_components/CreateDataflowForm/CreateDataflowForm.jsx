import React, { useContext, useRef } from 'react';

import * as Yup from 'yup';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import { isEmpty } from 'lodash';

import styles from './CreateDataflowForm.module.css';

import { Button } from 'ui/views/_components/Button';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export const CreateDataflowForm = ({ isFormReset, onCreate }) => {
  const form = useRef(null);
  const resources = useContext(ResourcesContext);
  const initialValues = { dataflowName: '', dataflowDescription: '', associatedObligation: '' };
  const validationSchema = Yup.object().shape({
    name: Yup.string().required(resources.messages['emptyNameValidationError']),
    description: Yup.string().required(resources.messages['emptyDescriptionValidationError'])
  });

  if (!isFormReset) {
    form.current.resetForm();
  }

  return (
    <Formik ref={form} initialValues={initialValues} validationSchema={validationSchema} onSubmit>
      {({ isSubmitting, errors, touched }) => (
        <Form>
          <fieldset>
            <div className={`formField${!isEmpty(errors.name) && touched.name ? ' error' : ''}`}>
              <Field name="name" type="text" placeholder={resources.messages['createDataflowName']} />
              <ErrorMessage className="error" name="name" component="div" />
            </div>
            <div className={`formField${!isEmpty(errors.description) && touched.description ? ' error' : ''}`}>
              <Field
                name="description"
                component="textarea"
                placeholder={resources.messages['createDataflowDescription']}
              />
              <ErrorMessage className="error" name="description" component="div" />
            </div>
            <div className={styles.search}>
              <Field
                className={styles.searchInput}
                disabled={true}
                name="obligation"
                placeholder={resources.messages['associatedObligation']}
                type="text"
              />
              <Button
                className={styles.searchButton}
                disabled={true}
                icon="search"
                label={resources.messages['search']}
                layout="simple"
              />
            </div>
          </fieldset>
          <fieldset>
            <div className={styles.wrapButtons}>
              <Button
                className={styles.resetButton}
                icon="cancel"
                label={resources.messages['reset']}
                layout="simple"
                type="reset"
              />
              <Button
                className={styles.submitButton}
                disabled={isSubmitting}
                icon="plus"
                label={resources.messages['createNewDataflow']}
                layout="simple"
                type="submit"
              />
            </div>
          </fieldset>
        </Form>
      )}
    </Formik>
  );
};
