import React, { useEffect, useState, useContext } from 'react';
import { withRouter } from 'react-router-dom';

import { isUndefined } from 'lodash';

import styles from './DataFlowColumn.module.css';

import { config } from 'conf';

import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Icon } from 'ui/views/_components/Icon';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { UserContext } from 'ui/views/_components/_context/UserContext';
import { UserService } from 'core/services/User';

const DataflowColumn = withRouter(
  ({
    navTitle,
    dataflowTitle,
    components = [],
    entity,
    createDataflowButtonTitle,
    subscribeButtonTitle,
    history,
    match
  }) => {
    const resources = useContext(ResourcesContext);
    const [isCustodian, setIsCustodian] = useState(false);
    const [subscribeDialogVisible, setSubscribeDialogVisible] = useState(false);
    const user = useContext(UserContext);

    useEffect(() => {
      if (!isUndefined(user.mainRole)) {
        setIsCustodian(UserService.hasPermission(user, [config.permissions.CUSTODIAN]));
      }
    }, [user]);

    console.log('user', user);

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
            />
          </div>
        )}
        <div className="navSection">
          {dataflowTitle && (
            <h4 className={styles.title}>
              <Icon icon="shoppingCart" />
              {dataflowTitle}
            </h4>
          )}

          {isCustodian && components.includes('createDataflow') && (
            <Button
              className={`${styles.columnButton} p-button-warning`}
              icon="plus"
              label={createDataflowButtonTitle}
            />
          )}

          <Button
            className={styles.columnButton}
            icon="plus"
            label={subscribeButtonTitle}
            onClick={() => {
              setVisibleHandler(setSubscribeDialogVisible, true);
            }}
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
