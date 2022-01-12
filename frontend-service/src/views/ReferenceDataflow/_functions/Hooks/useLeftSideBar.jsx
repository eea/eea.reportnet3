import { useContext, useLayoutEffect } from 'react';

import { LeftSideBarContext } from 'views/_functions/Contexts/LeftSideBarContext';

export const useLeftSideBar = (dataflowState, getLeftSidebarButtonsVisibility, manageDialogs) => {
  const leftSideBarContext = useContext(LeftSideBarContext);

  useLayoutEffect(() => {
    const buttonsVisibility = getLeftSidebarButtonsVisibility();

    const apiKeyBtn = {
      className: 'dataflow-api-key-help-step',
      icon: 'settings',
      isVisible: buttonsVisibility.apiKeyBtn,
      label: 'sidebarApiKeyBtn',
      onClick: () => manageDialogs('isApiKeyDialogVisible', true),
      title: 'sidebarApiKeyBtn'
    };

    const editBtn = {
      className: 'dataflow-edit-help-step',
      icon: 'edit',
      isVisible: buttonsVisibility.editBtn,
      label: 'edit',
      onClick: () => manageDialogs('isEditDialogVisible', true),
      title: 'edit'
    };

    const manageRequestersBtn = {
      className: 'dataflow-manage-rights-help-step',
      icon: 'userConfig',
      isVisible: buttonsVisibility.manageRequestersBtn,
      label: 'manageRequestersRights',
      onClick: () => manageDialogs('isManageRequestersDialogVisible', true),
      title: 'manageRequestersRights'
    };

    const propertiesBtn = {
      className: 'dataflow-properties-help-step',
      icon: 'infoCircle',
      isVisible: buttonsVisibility.propertiesBtn,
      label: 'properties',
      onClick: () => manageDialogs('isPropertiesDialogVisible', true),
      title: 'properties'
    };

    const reportingDataflowsBtn = {
      className: 'dataflow-properties-help-step',
      icon: 'clone',
      isVisible: buttonsVisibility.reportingDataflowsBtn,
      label: 'leftBarReportingDataflowsButton',
      onClick: () => manageDialogs('isReferencingDataflowsDialogVisible', true),
      title: 'leftBarReportingDataflowsButton'
    };

    const datasetsInfoBtn = {
      className: 'dataflow-help-datasets-info-step',
      icon: 'listClipboard',
      isVisible: buttonsVisibility.datasetsInfoBtn,
      label: 'datasetsInfo',
      onClick: () => manageDialogs('isDatasetsInfoDialogVisible', true),
      title: 'datasetsInfo'
    };

    const allButtons = [propertiesBtn, editBtn, apiKeyBtn, manageRequestersBtn, reportingDataflowsBtn, datasetsInfoBtn];

    leftSideBarContext.addModels(allButtons.filter(button => button.isVisible));
  }, [
    dataflowState.userRoles,
    dataflowState.status,
    dataflowState.datasetId,
    dataflowState.isAdmin,
    dataflowState.hasCustodianPermissions
  ]);
};
