import React, { useContext, useEffect } from 'react';

import primeIcons from 'assets/conf/prime.icons';
import styles from './SnapshotSliderBar.module.scss';

import { Formik, Field, Form } from 'formik';
import { IconComponent } from 'ui/views/_components/IconComponent';
import { isEmpty } from 'lodash';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Sidebar } from 'primereact/sidebar';
import { SnapshotContext } from '../../ReporterDataSet';
import { SnapshotList } from './_components/SnapshotList';
import * as Yup from 'yup';

const SnapshotSlideBar = ({ isVisible, setIsVisible, snapshotListData }) => {
  const snapshotContext = useContext(SnapshotContext);
  const resources = useContext(ResourcesContext);

  useEffect(() => {
    const bodySelector = document.querySelector('body');
    isVisible ? (bodySelector.style.overflowY = 'hidden') : (bodySelector.style.overflowY = 'auto');
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
      className={styles.sidebar}
      visible={isVisible}>
      <div className={styles.content}>
        <div className={styles.title}>
          <h3>{resources.messages.createSnapshotTitle}</h3>
        </div>
        <div className={`${styles.newContainer} ${styles.section}`}>
          <Formik
            initialValues={{ createSnapshot: '' }}
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
                  className={`${styles.createInputAndButtonWrapper} ${
                    !isEmpty(errors.createSnapshotDescription) && touched.createSnapshotDescription ? ' error' : ''
                  }`}>
                  <Field
                    type="text"
                    name="createSnapshotDescription"
                    placeholder={resources.messages.createSnapshotPlaceholder}
                  />
                  <div className={styles.createButtonWrapper}>
                    <button className={`${styles.createSnapshotButton} rp-btn primary`} type="submit">
                      <IconComponent icon={primeIcons.icons.plus} />
                    </button>
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
