import React, { useState, useContext } from 'react';

import styles from './DataFlowColumn.module.css';

import { config } from 'assets/conf';

import { Button } from 'primereact/button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { IconComponent } from 'ui/views/_components/IconComponent';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

const DataFlowColumn = ({ navTitle, dataFlowTitle, search = false, buttonTitle }) => {
  const resources = useContext(ResourcesContext);
  const [subscribeDialogVisible, setSubscribeDialogVisible] = useState(false);

  const setVisibleHandler = (fnUseState, visible) => {
    fnUseState(visible);
  };

  const onConfirmSubscribeHandler = () => {
    setSubscribeDialogVisible(false);
    HTTPRequester.get({
      url: window.env.REACT_APP_JSON ? '/subscribe/dataflow' : '/subscribe/dataflow',
      queryString: {}
    });
  };

  return (
    <div className="nav rep-col-12 rep-col-sm-3">
      <h2 className={styles.title}>{navTitle}</h2>
      {search && (
        <div className="navSection">
          <input
            type="text"
            id=""
            /* onKeyUp="" */
            className={styles.searchInput}
            placeholder={resources.messages['searchDataFlow']}
            title={resources.messages['typeDataFlowName']}
          />
        </div>
      )}
      <div className="navSection">
        {dataFlowTitle && (
          <h4 className={styles.title}>
            <IconComponent icon={config.icons.shoppingCart} />
            {dataFlowTitle}
          </h4>
        )}

        <Button
          icon={config.icons.plus}
          label={buttonTitle}
          className={styles.subscribeBtn}
          onClick={() => {
            setVisibleHandler(setSubscribeDialogVisible, true);
          }}
        />
        <ConfirmDialog
          onConfirm={onConfirmSubscribeHandler}
          onHide={() => setVisibleHandler(setSubscribeDialogVisible, false)}
          visible={subscribeDialogVisible}
          header={resources.messages['subscribeButtonTitle']}
          maximizable={false}
          labelConfirm={resources.messages['yes']}
          labelCancel={resources.messages['close']}>
          {resources.messages['subscribeDataFlow']}
        </ConfirmDialog>
      </div>
    </div>
  );
};

export { DataFlowColumn };
