import React, { useState, useContext } from 'react';
import { withRouter } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import styles from './DataFlowColumn.module.css';

import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Icon } from 'ui/views/_components/Icon';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { UserContext } from 'ui/views/_components/_context/UserContext';

const DataflowColumn = withRouter(
  ({ navTitle, dataflowTitle, components = [], entity, buttonTitle, history, match }) => {
    const resources = useContext(ResourcesContext);
    const [subscribeDialogVisible, setSubscribeDialogVisible] = useState(false);
    const user = useContext(UserContext);

    const setVisibleHandler = (fnUseState, visible) => {
      fnUseState(visible);
    };

    const onConfirmSubscribeHandler = () => {
      setSubscribeDialogVisible(false);
    };

    return (
      <div className="nav rep-col-12 rep-col-sm-3">
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
          {dataflowTitle && (
            <h4 className={styles.title}>
              <FontAwesomeIcon icon={AwesomeIcons('archive')} style={{ fontSize: '0.8rem' }} /> {dataflowTitle}
            </h4>
          )}

          <Button
            className={styles.subscribeBtn}
            icon="plus"
            label={buttonTitle}
            onClick={() => {
              setVisibleHandler(setSubscribeDialogVisible, true);
            }}
            disabled
          />
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
      </div>
    );
  }
);

export { DataflowColumn };
