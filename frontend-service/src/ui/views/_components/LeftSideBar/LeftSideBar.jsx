import React, { useState, useContext } from 'react';
import { withRouter, Link } from 'react-router-dom';

import styles from './LeftSideBar.module.css';

import { routes } from 'ui/routes';

import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { getUrl } from 'core/infrastructure/CoreUtils';

const LeftSideBar = withRouter(
  ({
    components = [],
    createDataflowButtonTitle,
    isCustodian,
    navTitle,
    onShowAddForm,
    style,
    subscribeButtonTitle
  }) => {
    const resources = useContext(ResourcesContext);

    const [subscribeDialogVisible, setSubscribeDialogVisible] = useState(false);

    const setVisibleHandler = (fnUseState, visible) => {
      fnUseState(visible);
    };

    const onConfirmSubscribeHandler = () => {
      setSubscribeDialogVisible(false);
    };

    return (
      <div className="nav rep-col-12 rep-col-xl-2">
        <h2 className={styles.title}>{navTitle}</h2>
        {components.includes('search') && (
          <div className="navSection">
            <input
              className={styles.searchInput}
              id=""
              placeholder={resources.messages['searchDataflow']}
              type="text"
              disabled
            />
          </div>
        )}
        <div className="navSection">
          {isCustodian && components.includes('createDataflow') ? (
            <React.Fragment>
              <Button
                className={`${styles.columnButton} p-button-primary`}
                icon="plus"
                label={createDataflowButtonTitle}
                onClick={() => onShowAddForm()}
                style={{ textAlign: 'left' }}
              />
              <Link to={getUrl(routes.CODELISTS, {}, true)}>
                <Button
                  className={styles.columnButton}
                  icon="plus"
                  label={resources.messages['manageCodelists']}
                  style={style}
                />
              </Link>
            </React.Fragment>
          ) : null}

          {/* <Button
            className={styles.columnButton}
            icon="plus"
            label={subscribeButtonTitle}
            onClick={() => {
              setVisibleHandler(setSubscribeDialogVisible, true);
            }}
            style={style}
            disabled
          />           */}
        </div>

        <ConfirmDialog
          header={resources.messages['subscribeButtonTitle']}
          maximizable={false}
          labelCancel={resources.messages['close']}
          labelConfirm={resources.messages['yes']}
          onConfirm={onConfirmSubscribeHandler}
          onHide={() => setVisibleHandler(setSubscribeDialogVisible, false)}
          visible={subscribeDialogVisible}>
          {resources.messages['subscribeDataflow']}
        </ConfirmDialog>
      </div>
    );
  }
);

export { LeftSideBar };
