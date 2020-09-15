/* eslint-disable react-hooks/exhaustive-deps */
import React, { useContext, useEffect, useRef, useState } from 'react';

import * as Yup from 'yup';
import { Formik, Field, Form } from 'formik';
import { isEmpty } from 'lodash';

import styles from './SnapshotSliderBar.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Sidebar } from 'primereact/sidebar';
import { SnapshotsList } from './_components/SnapshotsList';
import { Spinner } from 'ui/views/_components/Spinner';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'ui/views/_functions/Contexts/SnapshotContext';
import { DialogContext } from 'ui/views/_functions/Contexts/DialogContext';

const SnapshotSlideBar = ({
  isLoadingSnapshotListData,
  isReleaseVisible,
  isSnapshotDialogVisible,
  snapshotListData
}) => {
  const [slideBarStyle, setSlideBarStyle] = useState({});
  const dialogContext = useContext(DialogContext);
  const snapshotContext = useContext(SnapshotContext);
  const resources = useContext(ResourcesContext);
  const form = useRef(null);

  const isVisible = snapshotContext.isSnapshotsBarVisible;
  const setIsVisible = snapshotContext.setIsSnapshotsBarVisible;

  useEffect(() => {
    resetSlideBarPositionAndSize();
  }, [isVisible, isSnapshotDialogVisible]);

  useEffect(() => {
    showScrollingBar();
  }, [slideBarStyle]);

  useEffect(() => {
    window.addEventListener('resize', resetSlideBarPositionAndSize);
    return () => {
      window.removeEventListener('resize', resetSlideBarPositionAndSize);
    };
  });

  const showScrollingBar = () => {
    const bodySelector = document.querySelector('body');
    if (isVisible) {
      bodySelector.style.overflow = 'hidden';
    } else {
      if (dialogContext.open.length == 0) {
        bodySelector.style.overflow = 'hidden auto';
      }
    }
  };

  const resetSlideBarPositionAndSize = () => {
    const documentElement = document.compatMode === 'CSS1Compat' ? document.documentElement : document.body;

    const headerHeight = document.getElementById('header').clientHeight;

    setSlideBarStyle({
      height: `${documentElement.clientHeight - headerHeight}px`,
      top: `${headerHeight}px`
    });
  };

  const snapshotValidationSchema = Yup.object().shape({
    createSnapshotDescription: Yup.string().max(255, resources.messages['snapshotDescriptionValidationMax']).required()
  });

  if (isVisible) {
    form.current.resetForm();
  }

  return (
    <Sidebar
      baseZIndex={1900}
      blockScroll={true}
      className={styles.sidebar}
      onHide={() => setIsVisible()}
      position="right"
      visible={isVisible}
      style={slideBarStyle}>
      <div className={styles.content}>
        <div className={styles.title}>
          <h3>{resources.messages.createSnapshotTitle}</h3>
        </div>
        <div className={`${styles.newContainer} ${styles.section}`}>
          <Formik
            ref={form}
            initialValues={{ createSnapshotDescription: '' }}
            validationSchema={snapshotValidationSchema}
            onSubmit={values => {
              snapshotContext.snapshotDispatch({
                type: 'CREATE_SNAPSHOT',
                payload: {
                  description: values.createSnapshotDescription
                }
              });
              values.createSnapshotDescription = '';
            }}
            render={({ errors, touched }) => (
              <Form className={styles.createForm}>
                <div
                  className={`${styles.snapshotForm} formField ${styles.createInputAndButtonWrapper} ${
                    !isEmpty(errors.createSnapshotDescription) && touched.createSnapshotDescription ? ' error' : ''
                  }`}>
                  <Field
                    autoComplete="off"
                    className={styles.formField}
                    id="createSnapshotDescription"
                    maxLength={255}
                    name="createSnapshotDescription"
                    placeholder={resources.messages.createSnapshotPlaceholder}
                    type="text"
                  />
                  <label htmlFor="createSnapshotDescription" className="srOnly">
                    {resources.messages['createSnapshotPlaceholder']}
                  </label>
                  <div className={styles.createButtonWrapper}>
                    <Button
                      className={`${styles.createSnapshotButton} rp-btn secondary`}
                      tooltip={resources.messages.createSnapshotTooltip}
                      type="submit"
                      icon="plus"
                    />
                  </div>
                </div>
              </Form>
            )}
          />
        </div>
        {isLoadingSnapshotListData ? (
          <Spinner />
        ) : snapshotListData.length > 0 ? (
          <SnapshotsList snapshotListData={snapshotListData} isReleaseVisible={isReleaseVisible} />
        ) : (
          <h3>{resources.messages.snapshotsDoNotExist}</h3>
        )}
      </div>
    </Sidebar>
  );
};

export { SnapshotSlideBar };
