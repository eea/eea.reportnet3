/* eslint-disable react-hooks/exhaustive-deps */
import React, { useContext, useEffect } from 'react';

import primeIcons from 'assets/conf/prime.icons';
import styles from './SnapshotSliderBar.module.scss';

import { Formik, Field, Form } from 'formik';
import { IconComponent } from 'ui/views/_components/IconComponent';
import { isEmpty } from 'lodash';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { ScrollPanel } from 'primereact/scrollpanel';
import { Sidebar } from 'primereact/sidebar';
import { SnapshotContext } from '../../ReporterDataSet';
import { SnapshotList } from './_components/SnapshotList';
import * as Yup from 'yup';

const SnapshotSlideBar = ({ isVisible, setIsVisible, dataSetId, setSnapshotList, snapshotListData }) => {
  const snapshotContext = useContext(SnapshotContext);
  const resources = useContext(ResourcesContext);

  useEffect(() => {
    setSnapshotList();
  }, []);

  useEffect(() => {
    const bodySelector = document.querySelector('body');
    isVisible ? (bodySelector.style.overflowY = 'hidden') : (bodySelector.style.overflowY = 'auto');
  }, [isVisible]);

  const createSnapshotInputValidationSchema = Yup.object().shape({
    createSnapshotDescriptionInputName: Yup.string()
      .min(2, resources.messages['snapshotDescriptionValidationMin'])
      .max(100, resources.messages['snapshotDescriptionValidationMax'])
      .required(resources.messages['snapshotDescriptionValidationRequired'])
  });

  return (
    <Sidebar
      visible={isVisible}
      onHide={e => setIsVisible()}
      blockScroll={true}
      position="right"
      className={styles.sidebar}>
      <div className={styles.content}>
        <div className={styles.title}>
          <h3>{resources.messages.createSnapshotTitle}</h3>
        </div>
        <div className={`${styles.newContainer} ${styles.section}`}>
          <Formik
            initialValues={{ createSnapshot: '' }}
            validationSchema={createSnapshotInputValidationSchema}
            onSubmit={snapshotDescriptionInput =>
              snapshotContext.snapshotDispatch({
                type: 'create_snapshot',
                payload: {
                  description: snapshotDescriptionInput.createSnapshotDescriptionInputName
                }
              })
            }
            render={({ errors, touched, isSubmitting }) => (
              <Form className={styles.createForm}>
                <div
                  className={`${styles.createInuputAndButtonBox} ${
                    !isEmpty(errors.createSnapshotDescriptionInputName) && touched.createSnapshotDescriptionInputName
                      ? ' error'
                      : ''
                  }`}>
                  <Field
                    type="text"
                    name="createSnapshotDescriptionInputName"
                    placeholder={resources.messages.createSnapshotPlaceholder}
                  />
                  <div className={styles.createButtonBox}>
                    <button className={`${styles.createSnapshotButton} rp-btn primary`} type="submit">
                      <IconComponent icon={primeIcons.icons.plus} />
                    </button>
                  </div>
                </div>
                {errors.createSnapshotDescriptionInputName || touched.createSnapshotDescriptionInputName ? (
                  <div className="error">{errors.createSnapshotDescriptionInputName}</div>
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
