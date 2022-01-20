import { Fragment, useContext, useState } from 'react';

import { Button } from 'views/_components/Button';
import { Dashboard } from 'views/_components/Dashboard';
import { Dialog } from 'views/_components/Dialog';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const DatasetDashboardDialog = ({ disabled = false, levelErrorTypes, tableSchemas }) => {
  const resourcesContext = useContext(ResourcesContext);
  const [isDashboardDialogVisible, setIsDashboardDialogVisible] = useState(false);

  const renderButton = () => {
    return (
      <Button
        className={`p-button-rounded p-button-secondary-transparent dataset-dashboards-help-step ${
          !disabled && 'p-button-animated-blink'
        }`}
        disabled={disabled}
        icon="dashboard"
        label={resourcesContext.messages['dashboards']}
        onClick={() => setIsDashboardDialogVisible(true)}
      />
    );
  };

  const renderDashboardFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink p-button-right-aligned"
      icon="cancel"
      label={resourcesContext.messages['close']}
      onClick={() => setIsDashboardDialogVisible(false)}
    />
  );

  const renderDialog = () => {
    if (isDashboardDialogVisible) {
      return (
        <Dialog
          footer={renderDashboardFooter}
          header={resourcesContext.messages['titleDashboard']}
          onHide={() => setIsDashboardDialogVisible(false)}
          style={{ width: '70vw' }}
          visible={isDashboardDialogVisible}>
          <Dashboard
            levelErrorTypes={levelErrorTypes}
            refresh={isDashboardDialogVisible}
            tableSchemaNames={tableSchemas}
          />
        </Dialog>
      );
    }
  };
  return (
    <Fragment>
      {renderButton()}
      {renderDialog()}
    </Fragment>
  );
};
