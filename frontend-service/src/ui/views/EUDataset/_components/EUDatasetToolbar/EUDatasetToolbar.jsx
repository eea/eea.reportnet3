import React, { useContext, useEffect, useReducer, useRef, useState } from 'react';

import styles from './EUDatasetToolbar.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Menu } from 'primereact/menu';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

export const EUDatasetToolbar = ({ datasetHasErrors, handleDialogs, isRefreshHighlighted, onLoadDatasetSchema }) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  return (
    <div className={styles.ButtonsBar}>
      <Toolbar>
        <div className="p-toolbar-group-left">
          <Button
            className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink`}
            icon={'trash'}
            label={resources.messages['deleteDatasetData']}
            onClick={() => handleDialogs('deleteData', true)}
          />
        </div>
        <div className="p-toolbar-group-right">
          <Button
            className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink`}
            icon={'validate'}
            label={resources.messages['validate']}
            onClick={() => handleDialogs('validate', true)}
          />
          <Button
            className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink`}
            // disabled={!datasetHasErrors || isWebFormMMR}
            icon={'warning'}
            iconClasses={datasetHasErrors ? 'warning' : ''}
            label={resources.messages['showValidations']}
            onClick={() => handleDialogs('validate', true)}
          />
          <Button
            className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink`}
            // disabled={isWebFormMMR || !datasetHasData}
            icon={'dashboard'}
            label={resources.messages['dashboards']}
            onClick={() => handleDialogs('validate', true)}
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
