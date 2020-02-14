import React, { Component, useContext, useEffect, useReducer, useState } from 'react';
import { withRouter } from 'react-router-dom';
import { isEmpty, isUndefined } from 'lodash';
import styles from './Settings.module.scss';
import { routes } from 'ui/routes';
import { MainLayout } from 'ui/views/_components/Layout';
import { Title } from '../_components/Title/Title';
import { DataflowService } from 'core/services/Dataflow';
import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { dataflowReducer } from 'ui/views/_components/DataflowManagementForm/_functions/Reducers';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { TextUtils } from 'ui/views/_functions/Utils';
import { UserDesign } from './_components/UserDesign/UserDesign';
import { UserDefault } from './_components/UserDefault';
import { UserConfiguration } from './_components/UserConfiguration';

const Settings = withRouter(({ history, match }) => {
  const breadCrumbContext = useContext(BreadCrumbContext);
  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  const [dataflowData, setDataflowData] = useState();
  const [dataflowStatus, setDataflowStatus] = useState();
  const [isCustodian, setIsCustodian] = useState(false);
  const [isDataUpdated, setIsDataUpdated] = useState(false);
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [loading, setLoading] = useState(true);
  const [ConfigVisible, setConfigVisible] = useState(false);
  const [dataflowState, dataflowDispatch] = useReducer(dataflowReducer, {});
  const [config, setConfig] = useState('isVisible');

  const [settingsSectionLoaded, setSettingsSectionLoaded] = useState('');

  //Bread Crumbs settings
  useEffect(() => {
    breadCrumbContext.add([
      {
        label: '',
        icon: 'home',
        href: getUrl(routes.DATAFLOWS),
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      {
        label: resources.messages['settingsUser'],
        icon: 'user-profile',
        href: getUrl(routes.SETTINGS),
        command: () => history.push(getUrl(routes.SETTINGS))
      }
    ]);
  }, []);

  useEffect(() => {
    leftSideBarContext.addModels([
      {
        icon: 'palette',
        label: 'design',
        onClick: e => {
          showDispatch({
            type: 'SHOWUSERDESIGN',
            payload: {
              isOpenUserDesign: true,
              isOpenUserConfiguration: false,
              isOpenUserDefault: false
            }
          });
        },

        title: 'design'
      },
      {
        // href: getUrl(routes['CODELISTS']),
        icon: 'configs',
        label: 'configUserSettngs',
        onClick: e => {
          showDispatch({
            type: 'SHOWUSERCONFIGURATION',
            payload: {
              isOpenUserConfiguration: true,
              isOpenUserDefault: false,
              isOpenUserDesign: false
            }
          });
        },
        title: 'configUserSettngs'
      }
    ]);
  }, []);

  const initialState = {
    isOpenUserDefault: true,
    isOpenUserDesign: false,
    isOpenUserConfiguration: false
  };

  const reducer = (state, { type, payload }) => {
    switch (type) {
      case 'SHOWUSERDESIGN':
        return {
          ...state,
          isOpenUserDesign: (state.isOpenUserDesign = payload.isOpenUserDesign),
          isOpenUserConfiguration: (state.isOpenUserConfiguration = payload.isOpenUserConfiguration),
          isOpenUserDefault: (state.isOpenUserDefault = payload.isOpenUserDefault)
        };
      case 'SHOWUSERCONFIGURATION':
        return {
          ...state,
          isOpenUserConfiguration: (state.isOpenUserConfiguration = payload.isOpenUserConfiguration),
          isOpenUserDesign: (state.isOpenUserDesign = payload.isOpenUserDesign),
          isOpenUserDefault: (state.isOpenUserDefault = payload.isOpenUserDefault)
        };
    }
  };

  const [showState, showDispatch] = useReducer(reducer, initialState);

  console.log('showState', showState);

  const Show = () => {
    return (
      <>
        {/* <h1>{counterState.counter}</h1> */}

        {showState.isOpenUserDefault && (
          <div>
            <UserDefault />
          </div>
        )}

        {showState.isOpenUserDesign && (
          <div>
            <UserDesign />
          </div>
        )}

        {showState.isOpenUserConfiguration && (
          <div>
            <UserConfiguration />
          </div>
        )}
      </>
    );
  };

  //   useEffect(() => {
  //     if (isCustodian && dataflowStatus === DataflowConf.dataflowStatus['DESIGN']) {
  //       leftSideBarContext.addModels([
  //         {
  //           label: 'edit',
  //           icon: 'edit',
  //           onClick: e => {
  //             onShowEditForm();
  //             dataflowDispatch({ type: 'ON_SELECT_DATAFLOW', payload: match.params.dataflowId });
  //           },
  //           title: 'edit'
  //         },
  //         {
  //           label: 'manageRoles',
  //           icon: 'manageRoles',
  //           onClick: () => {
  //             onShowManageRolesDialog();
  //           },
  //           title: 'manageRoles'
  //         },
  //         {
  //           label: 'settings',
  //           icon: 'settings',
  //           onClick: e => {
  //             setIsActivePropertiesDialog(true);
  //           },
  //           show: true,
  //           title: 'settings'
  //         }
  //       ]);
  //     } else {
  //       leftSideBarContext.addModels([
  //         {
  //           label: 'settings',
  //           icon: 'settings',
  //           onClick: e => {
  //             setIsActivePropertiesDialog(true);
  //           },
  //           title: 'settings'
  //         }
  //       ]);
  //     }
  //   }, [isCustodian, dataflowStatus]);

  useEffect(() => {
    setLoading(true);
    onLoadDataflowsData();
    //onLoadReportingDataflow();
    // onLoadSchemasValidations();
  }, [match.params.dataflowId, isDataUpdated]);

  useEffect(() => {
    const refresh = notificationContext.toShow.find(
      notification => notification.key === 'ADD_DATACOLLECTION_COMPLETED_EVENT'
    );
    if (refresh) {
      onUpdateData();
    }
  }, [notificationContext]);

  if (isDeleteDialogVisible && document.getElementsByClassName('p-inputtext p-component').length > 0) {
    document.getElementsByClassName('p-inputtext p-component')[0].focus();
  }

  const onLoadDataflowsData = async () => {
    try {
      const allDataflows = await DataflowService.all();
      const dataflowInitialValues = {};
      allDataflows.accepted.forEach(element => {
        dataflowInitialValues[element.id] = { name: element.name, description: element.description, id: element.id };
      });
      dataflowDispatch({
        type: 'ON_INIT_DATA',
        payload: dataflowInitialValues
      });
    } catch (error) {
      console.error('dataFetch error: ', error);
    }
  };

  /*const onLoadReportingDataflow = async () => {
    try {
      const dataflow = await DataflowService.reporting(match.params.dataflowId);
      setDataflowData(dataflow);
      setDataflowStatus(dataflow.status);
      if (!isEmpty(dataflow.designDatasets)) {
        dataflow.designDatasets.forEach((schema, idx) => {
          schema.index = idx;
        });
        setDesignDatasetSchemas(dataflow.designDatasets);
        const datasetSchemaInfo = [];
        dataflow.designDatasets.map(schema => {
          datasetSchemaInfo.push({ schemaName: schema.datasetSchemaName, schemaIndex: schema.index });
        });
        setUpdatedDatasetSchema(datasetSchemaInfo);
      }
    } catch (error) {
      notificationContext.add({
        type: 'RELEASED_BY_ID_REPORTER_ERROR',
        content: {}
      });
      if (error.response.status === 401 || error.response.status === 403) {
        history.push(getUrl(routes.DATAFLOWS));
      }
    } finally {
      setLoading(false);
    }
  };

  //
  const onLoadSchemasValidations = async () => {
    setIsDataSchemaCorrect(await DataflowService.schemasValidation(match.params.dataflowId));
  };
*/

  /*const onShowEditForm = () => {
    setIsEditForm(true);
    setIsDataflowDialogVisible(true);
    setIsDataflowFormReset(true);
  };

  const onShowManageRolesDialog = () => {
    setIsActiveManageRolesDialog(true);
  };
*/

  const onUpdateData = () => {
    setIsDataUpdated(!isDataUpdated);
  };

  const layout = children => {
    return (
      <MainLayout
        leftSideBarConfig={{
          isCustodian,
          buttons: []
        }}>
        <div className="rep-container">{children}</div>

        {/* <UserSettings /> */}

        {/* 
          <UserDesign /> 
          {if (settingsSectionLoaded === '<UserDesign />') <UserDesign />
            else <UserSettings />}
        */}
      </MainLayout>
    );
  };

  return layout(
    <div className="rep-row">
      {/* <LeftSideBar
        subscribeButtonTitle={resources.messages['subscribeThisButton']}
        dataflowTitle={dataflowData.name}
        navTitle={resources.messages['dataflow']}
        components={[]}
        entity={`${config.permissions.DATAFLOW}${dataflowData.id}`}
        style={{ textAlign: 'left' }}
      /> */}
      <div className={`${styles.pageContent} rep-col-12 rep-col-sm-12`}>
        <Title
          title={
            !isUndefined(dataflowState[match.params.dataflowId])
              ? TextUtils.ellipsis(dataflowState[match.params.dataflowId].name)
              : null
          }
          subtitle={resources.messages['settingsUser']}
          icon="user-profile"
          iconSize="4rem"
        />
      </div>

      {Show()}
    </div>
  );
});

export { Settings };
