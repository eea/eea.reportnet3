import React, { useContext, useRef } from 'react';

import { Formik, Form, Field, ErrorMessage } from 'formik';
import { isPlainObject, isEmpty } from 'lodash';

import styles from './CreateDataflowForm.module.css';

import { config } from 'conf';

import { Button } from 'ui/views/_components/Button';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export const CreateDataflowForm = () => {
  const form = useRef(null);
  const resources = useContext(ResourcesContext);
  const initialValues = { dataflowName: '', dataflowDescription: '', associatedObligation: '' };
  return (
    <Formik ref={form} initialValues={initialValues}>
      {({ isSubmitting, setFieldValue, errors, touched }) => (
        <Form>
          <fieldset>
            <div className={`formField${!isEmpty(errors.description) && touched.description ? ' error' : ''}`}>
              <Field name="name" type="text" placeholder={resources.messages.createDataflowName} />
              <ErrorMessage className="error" name="name" component="div" />
            </div>
          </fieldset>
          <fieldset>
            <div className={styles.btnWrapper}>
              <Button type="reset" className="p-button-rounded" label={resources.messages.reset} layout="simple" />
              <Button
                type="submit"
                className="p-button-primary"
                disabled={isSubmitting}
                label={resources.messages.upload}
                layout="simple"
              />
            </div>
          </fieldset>
        </Form>
      )}
    </Formik>
  );
};
