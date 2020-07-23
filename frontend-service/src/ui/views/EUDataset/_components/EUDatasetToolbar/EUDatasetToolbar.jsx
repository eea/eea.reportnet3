import React, { useContext } from 'react';

import styles from './EUDatasetToolbar.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const EUDatasetToolbar = ({
  datasetHasErrors,
  datasetHasData,
  handleDialogs,
  isRefreshHighlighted,
  onRefresh
}) => {
  const resources = useContext(ResourcesContext);

  return (
    <div className={styles.toolbar}>
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
            disabled={!datasetHasErrors}
            icon={'warning'}
            iconClasses={datasetHasErrors ? 'warning' : ''}
            label={resources.messages['showValidations']}
            onClick={() => handleDialogs('validationList', true)}
          />
          <Button
            className={'p-button-rounded p-button-secondary-transparent p-button-animated-blink'}
            icon={'horizontalSliders'}
            label={resources.messages['qcRules']}
            onClick={() => handleDialogs('qcRules', true)}
          />
          <Button
            className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink`}
            disabled={!datasetHasData}
            icon={'dashboard'}
            label={resources.messages['dashboards']}
            onClick={() => handleDialogs('dashboard', true)}
          />
          <Button
            className={`p-button-rounded p-button-${
              isRefreshHighlighted ? 'primary' : 'secondary-transparent'
            } p-button-animated-blink`}
            icon={'refresh'}
            label={resources.messages['refresh']}
            onClick={() => onRefresh()}
          />
        </div>
      </Toolbar>
    </div>
  );
};
