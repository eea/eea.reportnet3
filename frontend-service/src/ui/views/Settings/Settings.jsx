import React, {Component, useContext, useEffect, useReducer, useState } from 'react';
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
import {Config} from './_components/Configuration'

const Settings = withRouter(({ history, match }) => {
  const breadCrumbContext = useContext(BreadCrumbContext);

  const leftSideBarContext = useContext(LeftSideBarContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);
  const notificationContext = useContext(NotificationContext);

  const [dataflowData, setDataflowData] = useState();
  const [dataflowStatus, setDataflowStatus] = useState();
  const [isCustodian, setIsCustodian] = useState(false);
  const [isDataUpdated, setIsDataUpdated] = useState(false);
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [loading, setLoading] = useState(true);
  const [ConfigVisible, setConfigVisible]= useState(false)
  const [dataflowState, dataflowDispatch] = useReducer(dataflowReducer, {});
  const [config, setConfig] = useState('isVisible')
  

  



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



  useEffect ( () => {
    leftSideBarContext.addModels([
      {
        icon: 'palette',
        label: 'design',
        onClick: (e) => {
          e.preventDefault();

         setConfig(!'isVisible')
        },
        title: 'design'
      },
      {
        href: getUrl(routes['CODELISTS']),
        icon: 'configs',
        label: 'configUserSettngs',
        onClick: e => {
          e.preventDefault();
          history.push(getUrl(routes['CODELISTS']));
        },
        title: 'configUserSettngs'
      }
    ]);
  },[]

  )


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
        <Config />
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
    </div>
  );
});

export { Settings };
