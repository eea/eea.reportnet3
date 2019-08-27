import React, { useState, useReducer } from 'react';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';

import styles from './App.module.css';
import { routes } from 'ui/routes';

import { DataFlowTasks } from 'ui/views/DataFlowTasks/DataFlowTasks';
import { DataCustodianDashboards } from 'ui/views/DataCustodianDashboards/DataCustodianDashboards';
import { DocumentationDataSet } from 'ui/views/DocumentationDataSet/DocumentationDataSet';
import { Login } from 'ui/views/Login';
import { ReporterDataSet } from 'ui/views/ReporterDataSet/ReporterDataSet';
import { ReportingDataFlow } from 'ui/views/ReportingDataFlow/ReportingDataFlow';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { PrivateRoute } from 'ui/views/_components/PrivateRoute';
import { UserContext } from 'ui/views/_components/_context/UserContext';
import { UserService } from 'core/services/User';
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
          <Router>
            <Switch>
              <Route exact path="/" component={Login} />
              <PrivateRoute exact path={routes.DATAFLOW_TASKS} component={DataFlowTasks} />
              <PrivateRoute exact path={routes.DATA_CUSTODIAN_DASHBOARDS} component={DataCustodianDashboards} />
              <PrivateRoute exact path={routes.REPORTING_DATAFLOW} component={ReportingDataFlow} />
              <PrivateRoute path={routes.REPORTER_DATASET} component={ReporterDataSet} />
              <PrivateRoute path={routes.DOCUMENTATION_DATASET} component={DocumentationDataSet} />
            </Switch>
          </Router>
        </ResourcesContext.Provider>
      </UserContext.Provider>
    </div>
  );
};

export default App;
