/* eslint-disable react-hooks/exhaustive-deps */
import { useContext, useEffect, useState } from 'react';

import styles from './SnapshotSliderBar.module.scss';

import isEmpty from 'lodash/isEmpty';

import { Button } from 'ui/views/_components/Button';
import { Sidebar } from 'primereact/sidebar';
import { SnapshotsList } from './_components/SnapshotsList';
import { Spinner } from 'ui/views/_components/Spinner';

import { DialogContext } from 'ui/views/_functions/Contexts/DialogContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'ui/views/_functions/Contexts/SnapshotContext';

const SnapshotSlideBar = ({ isLoadingSnapshotListData, isSnapshotDialogVisible, snapshotListData }) => {
  const [hasError, setHasError] = useState(false);
  const [inputValue, setInputValue] = useState('');
  const [slideBarStyle, setSlideBarStyle] = useState({});

  const dialogContext = useContext(DialogContext);
  const resources = useContext(ResourcesContext);
  const snapshotContext = useContext(SnapshotContext);

  const isVisible = snapshotContext.isSnapshotsBarVisible;
  const setIsVisible = snapshotContext.setIsSnapshotsBarVisible;

  useEffect(() => {
    resetSlideBarPositionAndSize();
    if (!isVisible) {
      setHasError(false);
      setInputValue('');
    }
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

  const hasCorrectDescriptionLength = description => {
    return description.length > 0 && description.length <= 255;
  };

  const onConfirmClick = () => {
    if (hasCorrectDescriptionLength(inputValue)) {
      snapshotContext.snapshotDispatch({
        type: 'CREATE_SNAPSHOT',
        payload: {
          description: inputValue
        }
      });
      setInputValue('');
      setHasError(false);
    } else {
      setHasError(true);
    }
  };

  const onPressEnter = event => {
    if (event.key === 'Enter') {
      event.preventDefault();
      onConfirmClick();
    }
  };

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
          <div className={styles.createForm}>
            <div
              className={`${styles.snapshotForm} formField ${styles.createInputAndButtonWrapper} ${
                hasError ? ' error' : ''
              }`}>
              <input
                autoComplete="off"
                className={styles.formField}
                id="createSnapshotDescription"
                maxLength={255}
                name="createSnapshotDescription"
                onBlur={e => !hasCorrectDescriptionLength(e.target.value) && setHasError(true)}
                onChange={e => setInputValue(e.target.value)}
                onFocus={() => setHasError(false)}
                placeholder={resources.messages.createSnapshotPlaceholder}
                type="text"
                onKeyDown={e => onPressEnter(e)}
                value={inputValue}
              />
              <label htmlFor="createSnapshotDescription" className="srOnly">
                {resources.messages['createSnapshotPlaceholder']}
              </label>
              <div className={styles.createButtonWrapper}>
                <Button
                  onClick={() => onConfirmClick()}
                  className={`${styles.createSnapshotButton} rp-btn secondary`}
                  tooltip={resources.messages.createSnapshotTooltip}
                  type="submit"
                  icon="plus"
                />
              </div>
            </div>
          </div>
        </div>
        {isLoadingSnapshotListData ? (
          <Spinner />
        ) : snapshotListData.length > 0 ? (
          <SnapshotsList snapshotListData={snapshotListData} />
        ) : (
          <h3>{resources.messages.snapshotsDoNotExist}</h3>
        )}
      </div>
    </Sidebar>
  );
};

export { SnapshotSlideBar };
