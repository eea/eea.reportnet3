import React, { useState, useReducer } from 'react';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';

import styles from './App.module.css';
import { routes } from 'ui/routes';

import { AccessPoint } from 'ui/views/AccessPoint';
import { DataflowTasks } from 'ui/views/DataFlowTasks/DataFlowTasks';
import { DataCustodianDashboards } from 'ui/views/DataCustodianDashboards/DataCustodianDashboards';
import { DatasetDesigner } from 'ui/views/DatasetDesigner/DatasetDesigner';
import { DocumentationDataset } from 'ui/views/DocumentationDataSet/DocumentationDataSet';
import { Eulogin } from 'ui/views/Eulogin';
import { LoadingProvider } from 'ui/views/_components/_provider/LoadingProvider';
import { Login } from 'ui/views/Login';
import { ReporterDataset } from 'ui/views/ReporterDataSet/ReporterDataSet';
import { ReportingDataflow } from 'ui/views/ReportingDataFlow/ReportingDataFlow';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { PrivateRoute } from 'ui/views/_components/PrivateRoute';
import { UserContext } from 'ui/views/_components/_context/UserContext';
import { userReducer } from 'ui/views/_components/_context/UserReducer';

import langResources from 'conf/messages.en.json';

const App = () => {
  const [resources] = useState({ ...langResources });
  const [state, dispatch] = useReducer(userReducer, {});
  return (
    <div className={styles.app}>
      <UserContext.Provider
        value={{
          ...state,
          onLogin: user => {
            dispatch({
              type: 'LOGIN',
              payload: {
                user
              }
            });
          },
          onLogout: () => {
            dispatch({
              type: 'LOGOUT',
              payload: {
                user: {}
              }
            });
          },
          onTokenRefresh: user => {
            dispatch({
              type: 'REFRESH_TOKEN',
              payload: {
                user
              }
            });
          }
        }}>
        <ResourcesContext.Provider value={resources}>
          <LoadingProvider>
            <Router>
              <Switch>
                <Route exact path="/" component={window.env.REACT_APP_EULOGIN == 'true' ? AccessPoint : Login} />
                <Route exact path={routes.EULOGIN} component={Eulogin} />
                <PrivateRoute exact path={routes.DATASET_SCHEMA} component={DatasetDesigner} />
                <PrivateRoute exact path={routes.DASHBOARDS} component={DataCustodianDashboards} />
                <PrivateRoute exact path={routes.DATAFLOW} component={ReportingDataflow} />
                <PrivateRoute exact path={routes.DATAFLOWS} component={DataflowTasks} />
                <PrivateRoute exact path={routes.DATASET} component={ReporterDataset} />
                <PrivateRoute exact path={routes.DOCUMENTS} component={DocumentationDataset} />
              </Switch>
            </Router>
          </LoadingProvider>
        </ResourcesContext.Provider>
      </UserContext.Provider>
    </div>
  );
};

export default App;
