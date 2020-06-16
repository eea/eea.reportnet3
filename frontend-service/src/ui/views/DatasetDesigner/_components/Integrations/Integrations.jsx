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

  const [integrationsList, setIntegrationsList] = useState([]);
  const [updatedData, setUpdatedData] = useState({});

  const getUpdatedData = data => setUpdatedData(IntegrationsUtils.parseIntegrationsList(data));

  const getIntegrationsList = data => setIntegrationsList(data);

  const renderIntegrationFooter = (
    <Fragment>
      <div className="p-toolbar-group-left">
        <Button
          className="p-button-secondary p-button-animated-blink"
          icon={'plus'}
          label={resources.messages['createExternalIntegration']}
          onClick={() => {
            manageDialogs('isIntegrationListDialogVisible', false, 'isIntegrationManageDialogVisible', true);
            setUpdatedData({});
          }}
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
          designerState={designerState}
          getUpdatedData={getUpdatedData}
          integrationsList={getIntegrationsList}
          manageDialogs={manageDialogs}
        />
      </Dialog>

      {isIntegrationManageDialogVisible && (
        <ManageIntegrations
          dataflowId={dataflowId}
          designerState={designerState}
          manageDialogs={manageDialogs}
          updatedData={updatedData}
          integrationsList={integrationsList}
        />
      )}
    </Fragment>
  );
};
