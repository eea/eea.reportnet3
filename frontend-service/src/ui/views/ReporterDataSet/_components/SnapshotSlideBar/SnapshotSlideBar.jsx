import React, { useContext, useEffect, useState } from 'react';

import styles from './SnapshotSliderBar.module.css';

import { Formik, Field, Form } from 'formik';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
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

  const createSnapshotInputValidationSchema = Yup.object().shape({
    createSnapshotDescriptionInputName: Yup.string()
      .min(2, resources.messages['snapshotDescriptionValidationMin'])
      .max(100, resources.messages['snapshotDescriptionValidationMax'])
      .required(resources.messages['snapshotDescriptionValidationRequired'])
  });

  return (
    <Sidebar visible={isVisible} onHide={e => setIsVisible()} position="right">
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
              <Form>
                <Field
                  type="text"
                  name="createSnapshotDescriptionInputName"
                  placeholder={resources.messages.createSnapshotPlaceholder}
                />
                {errors.createSnapshotDescriptionInputName || touched.createSnapshotDescriptionInputName ? (
                  <div className={styles.errors}>{errors.createSnapshotDescriptionInputName}</div>
                ) : null}
                <button className="rp-btn primary" type="submit">
                  {resources.messages.create}
                </button>
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
