import { useContext, useLayoutEffect } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';

import { LeftSideBarContext } from 'views/_functions/Contexts/LeftSideBarContext';

import { TextByDataflowTypeUtils } from 'views/_functions/Utils/TextByDataflowTypeUtils';

export const useLeftSideBar = (
  dataflowState,
  dataProviderId,
  getLeftSidebarButtonsVisibility,
  manageDialogs,
  representativeId
) => {
  const leftSideBarContext = useContext(LeftSideBarContext);

  useLayoutEffect(() => {
    if (!isEmpty(dataflowState.userRoles) || dataflowState.isAdmin) {
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
        onClick: () => manageDialogs('isReportingDataflowDialogVisible', true),
        title: 'edit'
      };

      const editBusinessBtn = {
        className: 'dataflow-edit-help-step',
        icon: 'edit',
        isVisible: buttonsVisibility.editBusinessBtn,
        label: 'edit',
        onClick: () => manageDialogs('isBusinessDataflowDialogVisible', true),
        title: 'edit'
      };

      const exportSchemaBtn = {
        className: 'dataflow-export-schema-help-step',
        icon: 'download',
        isVisible: buttonsVisibility.exportBtn,
        label: 'exportSchema',
        onClick: () => manageDialogs('isExportDialogVisible', true),
        title: 'exportSchema'
      };

      const manageRequestersBtn = {
        className: 'dataflow-manage-rights-help-step',
        icon: 'userConfig',
        isVisible: buttonsVisibility.manageRequestersBtn,
        label: 'manageRequestersRights',
        onClick: () => manageDialogs('isManageRequestersDialogVisible', true),
        title: 'manageRequestersRights'
      };

      const manageReportersBtn = {
        className: 'dataflow-manage-rights-help-step',
        icon: 'userConfig',
        isVisible: buttonsVisibility.manageReportersBtn,
        label: 'manageReportersRights',
        onClick: () => manageDialogs('isManageReportersDialogVisible', true),
        title: 'manageReportersRights'
      };

      const propertiesBtn = {
        className: 'dataflow-properties-help-step',
        icon: 'infoCircle',
        isVisible: buttonsVisibility.propertiesBtn,
        label: 'properties',
        onClick: () => manageDialogs('isPropertiesDialogVisible', true),
        title: 'properties'
      };

      const releaseableBtn = {
        className: 'dataflow-releasable-help-step',
        icon: 'released',
        isVisible: buttonsVisibility.releaseableBtn,
        label: 'releasingLeftSideBarButton',
        onClick: () => manageDialogs('isReleaseableDialogVisible', true),
        title: 'releasingLeftSideBarButton'
      };

      const showPublicInfoBtn = {
        className: 'dataflow-showPublicInfo-help-step',
        icon: 'lock',
        isVisible: buttonsVisibility.showPublicInfoBtn,
        label: 'publicStatusLeftSideBarButton',
        onClick: () => manageDialogs('isShowPublicInfoDialogVisible', true),
        title: 'publicStatusLeftSideBarButton'
      };

      const userListBtn = {
        className: 'dataflow-properties-help-step',
        icon: 'users',
        isVisible: buttonsVisibility.usersListBtn,
        label:
          ((isNil(dataProviderId) && dataflowState.isCustodian) ||
            (isNil(representativeId) && dataflowState.isObserver)) &&
          dataflowState.status === config.dataflowStatus.OPEN
            ? TextByDataflowTypeUtils.getKeyByDataflowType(dataflowState.dataflowType, 'userListBtnLabel')
            : 'dataflowUsersList',
        onClick: () => manageDialogs('isUserListVisible', true),
        title:
          ((isNil(dataProviderId) && dataflowState.isCustodian) ||
            (isNil(representativeId) && dataflowState.isObserver)) &&
          dataflowState.status === config.dataflowStatus.OPEN
            ? TextByDataflowTypeUtils.getKeyByDataflowType(dataflowState.dataflowType, 'userListBtnLabel')
            : 'dataflowUsersList'
      };

      const helpDeskBtn = {
        className: 'dataflow-help-desk-help-step',
        icon: 'listClipboard',
        isVisible: buttonsVisibility.helpDeskBtn,
        label: 'sidebarHelpDeskBtn',
        onClick: () => manageDialogs('isHelpDeskVisible', true),
        title: 'sidebarHelpDeskBtn'
      };

      //DON'T SORT ALPHABETICALLY
      const allButtons = [
        propertiesBtn,
        editBtn,
        editBusinessBtn,
        releaseableBtn,
        showPublicInfoBtn,
        exportSchemaBtn,
        apiKeyBtn,
        manageRequestersBtn,
        manageReportersBtn,
        userListBtn,
        helpDeskBtn
      ];

      leftSideBarContext.addModels(allButtons.filter(button => button.isVisible));
    }
  }, [
    dataflowState.userRoles,
    dataflowState.status,
    representativeId,
    dataflowState.datasetId,
    dataflowState.designDatasetSchemas.length,
    dataflowState.isAdmin
  ]);
};
