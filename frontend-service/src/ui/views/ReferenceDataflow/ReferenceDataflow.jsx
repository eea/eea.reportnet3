/* eslint-disable jsx-a11y/anchor-is-valid */
import { useContext, useEffect, useLayoutEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import styles from './ReferenceDataflow.module.scss';

import { config } from 'conf';
import { routes } from 'ui/routes';
import { MainLayout } from 'ui/views/_components/Layout';
import { Spinner } from 'ui/views/_components/Spinner';
import { Title } from 'ui/views/_components/Title';

import { UserService } from 'core/services/User';
import { ReferenceDataflowService } from 'core/services/ReferenceDataflow';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { dataflowDataReducer } from './_functions/Reducers/dataflowDataReducer';

import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';
import { useCheckNotifications } from 'ui/views/_functions/Hooks/useCheckNotifications';
import { useLeftSideBar } from './_functions/Hooks/useLeftSideBar';

import { CurrentPage } from 'ui/views/_functions/Utils';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { TextUtils } from 'ui/views/_functions/Utils';
import { BigButtonListReference } from './_components/BigButtonListReference/BigButtonListReference';
import { BigButton } from '../_components/BigButton/BigButton';

const ReferenceDataflow = withRouter(({ history, match }) => {
  const {
    params: { referenceDataflowId }
  } = match;

  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const dataflowInitialState = {
    requestStatus: 'idle',
    error: null,
    // anySchemaAvailableInPublic: false,
    // currentUrl: '',
    data: {},
    // dataProviderId: [],
    // dataProviderSelected: {},
    // deleteInput: '',
    description: '',
    // designDatasetSchemas: [],
    // formHasRepresentatives: false,
    // hasRepresentativesWithoutDatasets: false,
    // hasWritePermissions: false,
    // id: referenceDataflowId,
    // isApiKeyDialogVisible: false,
    // isCopyDataCollectionToEuDatasetLoading: false,
    // isCustodian: false,
    // isDataSchemaCorrect: [],
    // isDataUpdated: false,
    // isDeleteDialogVisible: false,
    // isEditDialogVisible: false,
    // isExportDialogVisible: false,
    // isExportEuDatasetLoading: false,
    // isExporting: false,
    // isFetchingData: false,
    // isImportLeadReportersVisible: false,
    // isManageRequestersDialogVisible: false,
    // isManageReportersDialogVisible: false,
    // isManageRolesDialogVisible: false,
    // isNationalCoordinator: false,
    // isObserver: false,
    // isPageLoading: true,
    // isPropertiesDialogVisible: false,
    // isReceiptLoading: false,
    // isReceiptOutdated: false,
    // isReleasable: false,
    // isReleaseableDialogVisible: false,
    // isReleaseDialogVisible: false,
    // isShowPublicInfoDialogVisible: false,
    // isSnapshotDialogVisible: false,
    // isUserListVisible: false,
    name: '',
    // obligations: {},
    // representativesImport: false,
    // restrictFromPublic: false,
    // showPublicInfo: false,
    status: ''
    // updatedDatasetSchema: [],
    // userRoles: [],
    // isUserRightManagementDialogVisible: false
  };

  const [dataflowState, dataflowDispatch] = useReducer(dataflowDataReducer, dataflowInitialState);

  useEffect(() => {
    dataflowDispatch({ type: 'LOADING_STARTED' });
    onLoadReportingDataflow();
  }, [dataflowState.isDataUpdated]);

  useBreadCrumbs({
    currentPage: CurrentPage.REFERENCE_DATAFLOW,
    referenceDataflowId,
    history,
    matchParams: match.params
  });

  const onLoadReportingDataflow = async () => {
    try {
      const referenceDataflowResponse = await ReferenceDataflowService.referenceDataflow(referenceDataflowId);
      const referenceDataflow = referenceDataflowResponse.data;

      dataflowDispatch({
        type: 'LOADING_SUCCESS',
        payload: {
          data: referenceDataflow,
          description: referenceDataflow.description,
          name: referenceDataflow.name,
          status: referenceDataflow.status
        }
      });
    } catch (error) {
      notificationContext.add({ type: 'LOADING_ERROR', error });
      // history.push(getUrl(routes.DATAFLOWS));
    }
  };

  const getLeftSidebarButtonsVisibility = () => {
    // if (isEmpty(dataflowState.data)) {
    //   return {
    //     apiKeyBtn: false,
    //     editBtn: false,
    //     exportBtn: false,
    //     manageRequestersBtn: false,
    //     propertiesBtn: false,
    //     releaseableBtn: false,
    //     usersListBtn: false
    //   };
    // }

    return {
      apiKeyBtn: /* isLeadDesigner || isLeadReporterOfCountry */ true,
      editBtn: /* isDesign && isLeadDesigner */ true,
      // exportBtn: isLeadDesigner && dataflowState.designDatasetSchemas.length > 0,
      // manageReportersBtn: isLeadReporterOfCountry,
      manageRequestersBtn: true, //dataflowState.isCustodian,
      propertiesBtn: true
      // releaseableBtn: !isDesign && isLeadDesigner,
      // showPublicInfoBtn: !isDesign && isLeadDesigner,
      // usersListBtn:
      //   isLeadReporterOfCountry ||
      //   isNationalCoordinatorOfCountry ||
      //   isReporterOfCountry ||
      //   ((dataflowState.isCustodian || dataflowState.isObserver) && !isNil(representativeId))
    };
  };

  useLeftSideBar(dataflowState, getLeftSidebarButtonsVisibility /* manageDialogs */);

  const layout = children => (
    <MainLayout leftSideBarConfig={{ isCustodian: dataflowState.isCustodian, buttons: [] }}>
      <div className="rep-container">{children}</div>
    </MainLayout>
  );

  // if (dataflowState.isPageLoading) return layout(<Spinner />);

  return layout(
    <div className="rep-row">
      <div className={`${styles.pageContent} rep-col-12 rep-col-sm-12`}>
        <Title
          icon="clone"
          iconSize="4rem"
          subtitle={resources.messages['referenceDataflowCrumbLabel']}
          title={dataflowState.name}
        />
      </div>
      <BigButtonListReference className="dataflow-big-buttons-help-step" />
    </div>
  );
});

export { ReferenceDataflow };
