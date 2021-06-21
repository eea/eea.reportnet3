/* eslint-disable react-hooks/exhaustive-deps */
import { useContext, useEffect, useState } from 'react';

import styles from './SnapshotSliderBar.module.scss';

import { Button } from 'ui/views/_components/Button';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import ReactTooltip from 'react-tooltip';
import { Sidebar } from 'primereact/sidebar';
import { SnapshotsList } from './_components/SnapshotsList';
import { Spinner } from 'ui/views/_components/Spinner';

import { DialogContext } from 'ui/views/_functions/Contexts/DialogContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'ui/views/_functions/Contexts/SnapshotContext';

const SnapshotSlideBar = ({ isLoadingSnapshotListData, isSnapshotDialogVisible, snapshotListData }) => {
  const dialogContext = useContext(DialogContext);
  const resources = useContext(ResourcesContext);
  const snapshotContext = useContext(SnapshotContext);

  const [hasError, setHasError] = useState(false);
  const [slideBarStyle, setSlideBarStyle] = useState({});
  const [inputValue, setInputValue] = useState(snapshotContext.snapshotState.description);

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
    if (snapshotContext.snapshotState.description === '') {
      setInputValue('');
    }
  }, [snapshotContext.snapshotState.description]);

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
      if (dialogContext.open.length === 0) {
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
      style={slideBarStyle}
      visible={isVisible}>
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
              <InputTextarea
                autoComplete="off"
                className={styles.formField}
                collapsedHeight={90}
                id="createSnapshotDescription"
                maxLength={255}
                name="createSnapshotDescription"
                onChange={e => setInputValue(e.target.value)}
                onKeyDown={e => onPressEnter(e)}
                placeholder={resources.messages.createSnapshotPlaceholder}
                rows={10}
                type="text"
                value={inputValue}
              />
              <label className="srOnly" htmlFor="createSnapshotDescription">
                {resources.messages['createSnapshotPlaceholder']}
              </label>
              <div className={styles.createButtonWrapper} data-for="saveCopy" data-tip>
                <Button
                  className={`${styles.createSnapshotButton} rp-btn secondary`}
                  disabled={!hasCorrectDescriptionLength(inputValue)}
                  icon="plus"
                  onClick={() => onConfirmClick()}
                  type="submit"
                />
              </div>
              <ReactTooltip className={styles.tooltip} effect="solid" id="saveCopy" place="left">
                {inputValue.length === 0
                  ? resources.messages['snapshotsEmtpyDescription']
                  : inputValue.length > 255
                  ? resources.messages['snapshotsWrongLengthDescription']
                  : resources.messages.createSnapshotTooltip}
              </ReactTooltip>
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
