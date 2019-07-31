import React, { useState, useEffect, useContext, Suspense } from 'react';

import PropTypes from 'prop-types';

import styles from './DataFlowColumn.module.css';

import { config } from 'assets/conf';

import { Button } from 'primereact/button';
import { ButtonsBar } from 'ui/views/_components/ButtonsBar';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { IconComponent } from 'ui/views/_components/IconComponent';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

const DataFlowColumn = ({ navTitle, dataFlowTitle, search = false }) => {
  const resources = useContext(ResourcesContext);
  const [subscribeDialogVisible, setSubscribeDialogVisible] = useState(false);

  console.log('Start DataFlowColumn...');

  const setVisibleHandler = (fnUseState, visible) => {
    fnUseState(visible);
  };

  const onConfirmSubscribeHandler = () => {
    console.log('onConfirmSubscribeHandler');
    setSubscribeDialogVisible(false);
    HTTPRequester.get({ url: '/subscribe/dataflow', queryString: {} });
    console.log('/subscribe/dataflow');
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
            className=""
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
          label={resources.messages['subscribeButton']}
          className="p-button-primary"
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
