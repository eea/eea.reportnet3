import React, { useContext, useEffect, useReducer, useRef, useState } from 'react';

import styles from './EUDatasetToolbar.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Menu } from 'primereact/menu';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

export const EUDatasetToolbar = ({
  datasetHasData,
  datasetHasErrors,
  hasWritePermissions,
  isRefreshHighlighted,
  isWebFormMMR,
  onLoadDatasetSchema,
  onSetVisible,
  setDashDialogVisible,
  setDeleteDialogVisible,
  setValidateDialogVisible,
  setValidationsVisible
}) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  return (
    <div className={styles.ButtonsBar}>
      <Toolbar>
        <div className="p-toolbar-group-left">
          <Button
            className={`p-button-rounded p-button-secondary-transparent ${
              !hasWritePermissions || isWebFormMMR ? null : 'p-button-animated-blink'
            }`}
            icon={'trash'}
            label={resources.messages['deleteDatasetData']}
            disabled={!hasWritePermissions || isWebFormMMR}
            onClick={() => onSetVisible(setDeleteDialogVisible, true)}
          />
        </div>
        <div className="p-toolbar-group-right">
          <Button
            className={`p-button-rounded p-button-secondary-transparent ${
              !hasWritePermissions || isWebFormMMR || !datasetHasData ? null : 'p-button-animated-blink'
            }`}
            disabled={!hasWritePermissions || isWebFormMMR || !datasetHasData}
            icon={'validate'}
            label={resources.messages['validate']}
            onClick={() => onSetVisible(setValidateDialogVisible, true)}
            ownButtonClasses={null}
            iconClasses={null}
          />
          <Button
            className={`p-button-rounded p-button-secondary-transparent ${
              !datasetHasErrors || isWebFormMMR ? null : 'p-button-animated-blink'
            }`}
            disabled={!datasetHasErrors || isWebFormMMR}
            icon={'warning'}
            label={resources.messages['showValidations']}
            onClick={() => onSetVisible(setValidationsVisible, true)}
            ownButtonClasses={null}
            iconClasses={datasetHasErrors ? 'warning' : ''}
          />
          <Button
            className={`p-button-rounded p-button-secondary-transparent ${
              isWebFormMMR || !datasetHasData ? null : 'p-button-animated-blink'
            }`}
            disabled={isWebFormMMR || !datasetHasData}
            icon={'dashboard'}
            label={resources.messages['dashboards']}
            onClick={() => onSetVisible(setDashDialogVisible, true)}
          />
          <Button
            className={`p-button-rounded p-button-${
              isRefreshHighlighted ? 'primary' : 'secondary-transparent'
            } p-button-animated-blink`}
            icon={'refresh'}
            label={resources.messages['refresh']}
            onClick={() => onLoadDatasetSchema()}
          />
        </div>
      </Toolbar>
    </div>
  );
};
