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

    const helpDeskBtn = {
      className: 'dataflow-help-desk-help-step',
      // icon: 'thList',
      icon: 'listClipboard',
      isVisible: buttonsVisibility.helpDeskBtn,
      // label: 'datasetsInfo',
      label: 'sidebarHelpDeskBtn',
      onClick: () => manageDialogs('isHelpDeskVisible', true),
      // title: 'datasetsInfo'
      title: 'sidebarHelpDeskBtn'
    };

    const allButtons = [propertiesBtn, editBtn, apiKeyBtn, manageRequestersBtn, reportingDataflowsBtn, helpDeskBtn];

    leftSideBarContext.addModels(allButtons.filter(button => button.isVisible));
  }, [
    dataflowState.userRoles,
    dataflowState.status,
    dataflowState.datasetId,
    dataflowState.isAdmin,
    dataflowState.isCustodian
  ]);
};
