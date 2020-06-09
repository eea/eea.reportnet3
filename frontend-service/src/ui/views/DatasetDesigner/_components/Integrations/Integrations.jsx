import React, { Fragment, useContext, useState } from 'react';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { IntegrationsList } from './_components/IntegrationsList';
import { ManageIntegrations } from './_components/ManageIntegrations';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { IntegrationsUtils } from './_functions/Utils/IntegrationsUtils';

export const Integrations = ({ dataflowId, designerState, manageDialogs }) => {
  const { isIntegrationListDialogVisible, isIntegrationManageDialogVisible } = designerState;

  const resources = useContext(ResourcesContext);

  const [updatedData, setUpdatedData] = useState({});

  const getUpdatedData = data => setUpdatedData(IntegrationsUtils.parseIntegrationsList(data));

  const renderIntegrationFooter = (
    <Fragment>
      <div className="p-toolbar-group-left">
        <Button
          className="p-button-secondary p-button-animated-blink"
          icon={'plus'}
          label={resources.messages['createExternalIntegration']}
          onClick={() =>
            manageDialogs('isIntegrationListDialogVisible', false, 'isIntegrationManageDialogVisible', true)
          }
        />
      </div>
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() => manageDialogs('isIntegrationListDialogVisible', false)}
      />
    </Fragment>
  );

  return (
    <Fragment>
      <Dialog
        footer={renderIntegrationFooter}
        header={resources.messages['externalIntegrations']}
        onHide={() => manageDialogs('isIntegrationListDialogVisible', false)}
        style={{ width: '70%' }}
        visible={isIntegrationListDialogVisible}>
        <IntegrationsList
          dataflowId={dataflowId}
          designerState={designerState}
          getUpdatedData={getUpdatedData}
          manageDialogs={manageDialogs}
        />
      </Dialog>

      {isIntegrationManageDialogVisible && (
        <ManageIntegrations designerState={designerState} manageDialogs={manageDialogs} updatedData={updatedData} />
      )}
    </Fragment>
  );
};
