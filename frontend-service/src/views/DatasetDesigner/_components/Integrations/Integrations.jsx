import { Fragment, useContext, useState } from 'react';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { IntegrationsList } from './_components/IntegrationsList';
import { ManageIntegrations } from 'views/_components/ManageIntegrations';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { IntegrationsUtils } from './_functions/Utils/IntegrationsUtils';

export const Integrations = ({ dataflowId, datasetId, designerState, manageDialogs, onUpdateData }) => {
  const { isIntegrationListDialogVisible, isIntegrationManageDialogVisible } = designerState;

  const resourcesContext = useContext(ResourcesContext);

  const [integrationsList, setIntegrationsList] = useState([]);
  const [isUpdating, setIsUpdating] = useState(false);

  const [isCreating, setIsCreating] = useState(false);
  const [needsRefresh, setNeedsRefresh] = useState(true);
  const [updatedData, setUpdatedData] = useState({});

  const getIntegrationsList = data => setIntegrationsList(data);

  const getUpdatedData = data => setUpdatedData(IntegrationsUtils.parseIntegration(data));

  const onCloseListModal = () => {
    manageDialogs('isIntegrationListDialogVisible', false);
    refreshList(true);
  };

  const refreshList = value => setNeedsRefresh(value);

  const renderIntegrationFooter = (
    <Fragment>
      <div className="p-toolbar-group-left">
        <Button
          className="p-button-secondary p-button-animated-blink"
          icon={'plus'}
          label={resourcesContext.messages['createExternalIntegration']}
          onClick={() => {
            manageDialogs('isIntegrationManageDialogVisible', true);
            setUpdatedData({});
          }}
        />
      </div>
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon={'cancel'}
        label={resourcesContext.messages['close']}
        onClick={() => onCloseListModal()}
      />
    </Fragment>
  );

  return (
    <Fragment>
      {isIntegrationListDialogVisible && (
        <Dialog
          footer={renderIntegrationFooter}
          header={resourcesContext.messages['externalIntegrations']}
          onHide={() => onCloseListModal()}
          style={{ width: '70%' }}
          visible={isIntegrationListDialogVisible}>
          <IntegrationsList
            dataflowId={dataflowId}
            designerState={designerState}
            getUpdatedData={getUpdatedData}
            integrationsList={getIntegrationsList}
            isCreating={isCreating}
            isUpdating={isUpdating}
            manageDialogs={manageDialogs}
            needsRefresh={needsRefresh}
            onUpdateDesignData={onUpdateData}
            refreshList={refreshList}
            setIsCreating={setIsCreating}
            setIsUpdating={setIsUpdating}
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
          setIsCreating={setIsCreating}
          setIsUpdating={setIsUpdating}
          state={designerState}
          updatedData={updatedData}
        />
      )}
    </Fragment>
  );
};
