/* eslint-disable react-hooks/exhaustive-deps */
import React, { useContext, useEffect } from 'react';
import { Formik, Field, Form } from 'formik';
import * as Yup from 'yup';

import { isEmpty } from 'lodash';

import styles from './SnapshotSliderBar.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Sidebar } from 'primereact/sidebar';
import { SnapshotList } from './_components/SnapshotList';

import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { SnapshotContext } from 'ui/views/_components/_context/SnapshotContext';

const SnapshotSlideBar = ({ isVisible, setIsVisible, snapshotListData }) => {
  const snapshotContext = useContext(SnapshotContext);
  const resources = useContext(ResourcesContext);

  useEffect(() => {
    const bodySelector = document.querySelector('body');
    isVisible ? (bodySelector.style.overflow = 'hidden') : (bodySelector.style.overflow = 'hidden auto');
  }, [isVisible]);

  const snapshotValidationSchema = Yup.object().shape({
    createSnapshotDescription: Yup.string()
      .min(2, resources.messages['snapshotDescriptionValidationMin'])
      .max(100, resources.messages['snapshotDescriptionValidationMax'])
      .required(resources.messages['snapshotDescriptionValidationRequired'])
  });

  return (
    <Sidebar
      blockScroll={true}
      className={styles.sidebar}
      onHide={e => setIsVisible()}
      position="right"
      visible={isVisible}>
      <div className={styles.content}>
        <div className={styles.title}>
          <h3>{resources.messages.createSnapshotTitle}</h3>
        </div>
        <div className={`${styles.newContainer} ${styles.section}`}>
          <Formik
            initialValues={{ createSnapshotDescription: '' }}
            validationSchema={snapshotValidationSchema}
            onSubmit={values => {
              snapshotContext.snapshotDispatch({
                type: 'create_snapshot',
                payload: {
                  description: values.createSnapshotDescription
                }
              });
              values.createSnapshotDescription = '';
            }}
            render={({ errors, touched, isSubmitting }) => (
              <Form className={styles.createForm}>
                <div
                  className={`${styles.snapshotForm} formField ${styles.createInputAndButtonWrapper} ${
                    !isEmpty(errors.createSnapshotDescription) && touched.createSnapshotDescription ? ' error' : ''
                  }`}>
                  <Field
                    type="text"
                    name="createSnapshotDescription"
                    placeholder={resources.messages.createSnapshotPlaceholder}
                  />
                  <div className={styles.createButtonWrapper}>
                    <Button
                      tooltip={resources.messages.createSnapshotTooltip}
                      className={`${styles.createSnapshotButton} rp-btn default`}
                      type="submit"
                      icon="plus"
                    />
                  </div>
                </div>
                {errors.createSnapshotDescription || touched.createSnapshotDescription ? (
                  <div className="error">{errors.createSnapshotDescription}</div>
                ) : null}
              </Form>
            )}
          />
        </div>
        <SnapshotList snapshotListData={snapshotListData} />
      </div>
    </Sidebar>
  );
};

export { SnapshotSlideBar };
