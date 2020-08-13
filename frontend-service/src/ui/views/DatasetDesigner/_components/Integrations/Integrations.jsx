import React, { Fragment, useContext, useState } from 'react';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { IntegrationsList } from './_components/IntegrationsList';
import { ManageIntegrations } from 'ui/views/_components/ManageIntegrations';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { IntegrationsUtils } from './_functions/Utils/IntegrationsUtils';

export const Integrations = ({ dataflowId, datasetId, designerState, manageDialogs, onUpdateData }) => {
  const { isIntegrationListDialogVisible, isIntegrationManageDialogVisible } = designerState;

  const resources = useContext(ResourcesContext);

  const [integrationsList, setIntegrationsList] = useState([]);
  const [needsRefresh, setNeedsRefresh] = useState(true);
  const [updatedData, setUpdatedData] = useState({});

  const getIntegrationsList = data => setIntegrationsList(data);

  const getUpdatedData = data => setUpdatedData(IntegrationsUtils.parseIntegration(data));

  const refreshList = value => setNeedsRefresh(value);

  const renderIntegrationFooter = (
    <Fragment>
      <div className="p-toolbar-group-left">
        <Button
          className="p-button-secondary p-button-animated-blink"
          icon={'plus'}
          label={resources.messages['createExternalIntegration']}
          onClick={() => {
            manageDialogs('isIntegrationManageDialogVisible', true);
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
      {isIntegrationListDialogVisible && (
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
            integrationsList={getIntegrationsList}
            manageDialogs={manageDialogs}
            needsRefresh={needsRefresh}
            onUpdateDesignData={onUpdateData}
            refreshList={refreshList}
          />
        </Dialog>
      )}

      {isIntegrationManageDialogVisible && (
        <ManageIntegrations
          dataflowId={dataflowId}
          datasetId={datasetId}
          integrationsList={integrationsList}
          manageDialogs={manageDialogs}
          onUpdateData={onUpdateData}
          refreshList={refreshList}
          state={designerState}
          updatedData={updatedData}
        />
      )}
    </Fragment>
  );
};
