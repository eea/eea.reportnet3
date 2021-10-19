/* eslint-disable react-hooks/exhaustive-deps */
import { useContext, useEffect, useState } from 'react';

import styles from './SnapshotSliderBar.module.scss';

import { config } from 'conf';

import { Button } from 'views/_components/Button';
import { InputTextarea } from 'views/_components/InputTextarea';
import ReactTooltip from 'react-tooltip';
import { Sidebar } from 'primereact/sidebar';
import { SnapshotsList } from './_components/SnapshotsList';
import { Spinner } from 'views/_components/Spinner';
import { CharacterCounter } from 'views/_components/CharacterCounter';

import { DialogContext } from 'views/_functions/Contexts/DialogContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'views/_functions/Contexts/SnapshotContext';

const SnapshotSlideBar = ({ isLoadingSnapshotListData, isSnapshotDialogVisible, snapshotListData }) => {
  const dialogContext = useContext(DialogContext);
  const resourcesContext = useContext(ResourcesContext);
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
    return description.length > 0 && description.length <= config.MAX_ATTACHMENT_SIZE;
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
          <h3>{resourcesContext.messages.createSnapshotTitle}</h3>
        </div>
        <div className={`${styles.newContainer} ${styles.section}`}>
          <div className={styles.createForm}>
            <div
              className={`${styles.snapshotForm} formField ${styles.createInputAndButtonWrapper} ${
                hasError ? ' error' : ''
              }`}>
              <div className={styles.descriptionWrapper}>
                <InputTextarea
                  autoComplete="off"
                  className={styles.formField}
                  collapsedHeight={90}
                  id="createSnapshotDescription"
                  maxLength={config.INPUT_MAX_LENGTH}
                  name="createSnapshotDescription"
                  onChange={e => setInputValue(e.target.value)}
                  onKeyDown={e => onPressEnter(e)}
                  placeholder={resourcesContext.messages.createSnapshotPlaceholder}
                  rows={10}
                  type="text"
                  value={inputValue}
                />
                <CharacterCounter
                  currentLength={inputValue.length}
                  maxLength={config.INPUT_MAX_LENGTH}
                  style={{ position: 'relative', top: '0.25rem' }}
                />
                <label className="srOnly" htmlFor="createSnapshotDescription">
                  {resourcesContext.messages['createSnapshotPlaceholder']}
                </label>
              </div>
              <div className={styles.createButtonWrapper} data-for="saveCopy" data-tip>
                <Button
                  className={`${styles.createSnapshotButton} rp-btn secondary`}
                  disabled={!hasCorrectDescriptionLength(inputValue)}
                  icon="plus"
                  onClick={onConfirmClick}
                  type="submit"
                />
              </div>
              <ReactTooltip border={true} className={styles.tooltip} effect="solid" id="saveCopy" place="left">
                {inputValue.length === 0
                  ? resourcesContext.messages['snapshotsEmptyDescription']
                  : inputValue.length > config.INPUT_MAX_LENGTH
                  ? resourcesContext.messages['snapshotsWrongLengthDescription']
                  : resourcesContext.messages.createSnapshotTooltip}
              </ReactTooltip>
            </div>
          </div>
        </div>
        {isLoadingSnapshotListData ? (
          <Spinner className={styles.spinner} />
        ) : snapshotListData.length > 0 ? (
          <SnapshotsList snapshotListData={snapshotListData} />
        ) : (
          <h3>{resourcesContext.messages.snapshotsDoNotExist}</h3>
        )}
      </div>
    </Sidebar>
  );
};

export { SnapshotSlideBar };
