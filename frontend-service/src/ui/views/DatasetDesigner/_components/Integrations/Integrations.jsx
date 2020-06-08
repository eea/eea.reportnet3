import React, { Fragment, useContext } from 'react';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { IntegrationsList } from './_components/IntegrationsList';
import { ManageIntegrations } from './_components/ManageIntegrations';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const Integrations = ({ dataflowId, designerState, manageDialogs }) => {
  const resources = useContext(ResourcesContext);

  const renderIntegrationFooter = (
    <Fragment>
      <div className="p-toolbar-group-left">
        <Button
          className="p-button-secondary p-button-animated-blink"
          icon={'plus'}
          label={resources.messages['addIntegration']}
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
        header={resources.messages['integration']}
        onHide={() => manageDialogs('isIntegrationListDialogVisible', false)}
        style={{ width: '70%' }}
        visible={designerState.isIntegrationListDialogVisible}>
        <IntegrationsList dataflowId={dataflowId} designerState={designerState} />
      </Dialog>

      {/* <Dialog
        footer={renderIntegrationFooter}
        header={resources.messages['integration']}
        onHide={() => manageDialogs('isIntegrationListDialogVisible', false)}
        style={{ width: '70%' }}
        visible={true}> */}
      <ManageIntegrations designerState={designerState} manageDialogs={manageDialogs} />
      {/* </Dialog> */}
    </Fragment>
  );
};
