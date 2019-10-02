import React, { useState, useContext } from 'react';
import { withRouter } from 'react-router-dom';

import styles from './DataFlowColumn.module.css';

import { config } from 'conf';

import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Icon } from 'ui/views/_components/Icon';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { UserContext } from 'ui/views/_components/_context/UserContext';
import { UserService } from 'core/services/User';

const DataFlowColumn = withRouter(
  ({ navTitle, dataFlowTitle, components = [], entity, buttonTitle, history, match }) => {
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
              placeholder={resources.messages['searchDataFlow']}
              type="text"
            />
          </div>
        )}
        <div className="navSection">
          {dataFlowTitle && (
            <h4 className={styles.title}>
              <Icon icon="shoppingCart" />
              {dataFlowTitle}
            </h4>
          )}

          <Button
            className={styles.subscribeBtn}
            icon="plus"
            label={buttonTitle}
            onClick={() => {
              setVisibleHandler(setSubscribeDialogVisible, true);
            }}
          />
          {components.includes('dashboard') &&
          UserService.hasPermission(user, [config.permissions.CUSTODIAN], entity) ? (
            <>
              <hr />
              <Button
                className={styles.subscribeBtn}
                icon="dashboard"
                label={'View Dashboard'}
                onClick={e => {
                  e.preventDefault();
                  history.push(`/reporting-data-flow/${match.params.dataFlowId}/data-custodian-dashboards/`);
                }}
              />
            </>
          ) : null}
          <ConfirmDialog
            header={resources.messages['subscribeButtonTitle']}
            maximizable={false}
            labelCancel={resources.messages['close']}
            labelConfirm={resources.messages['yes']}
            onConfirm={onConfirmSubscribeHandler}
            onHide={() => setVisibleHandler(setSubscribeDialogVisible, false)}
            visible={subscribeDialogVisible}>
            {resources.messages['subscribeDataFlow']}
          </ConfirmDialog>
        </div>
      </div>
    );
  }
);

export { DataFlowColumn };
