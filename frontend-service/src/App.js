import React, { useState, useReducer } from 'react';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';

import styles from './App.module.css';
import { routes } from 'ui/routes';

import { DataFlowTasks } from 'ui/views/DataFlowTasks/DataFlowTasks';
import { DocumentationDataSet } from 'ui/views/DocumentationDataSet/DocumentationDataSet';
import { Login } from 'ui/views/Login';
import { ReporterDataSet } from 'ui/views/ReporterDataSet/ReporterDataSet';
import { ReportingDataFlow } from 'ui/views/ReportingDataFlow/ReportingDataFlow';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { PrivateRoute } from 'ui/views/_components/PrivateRoute';
import { UserContext } from 'ui/views/_components/_context/UserContext';

import langResources from 'conf/messages.en.json';

function reducer(state, action) {
  switch (action.type) {
    case 'login':
      return {
        ...state,
        logged: true
      };
    case 'logout':
      return {
        ...state,
        logged: false
      };
    case 'refreshToken':
      break;

    default:
      return state;
  }
}

const App = () => {
  const [resources] = useState({ ...langResources });
  const userInitialContext = {
    logged: false
  };
  const [state, dispatch] = useReducer(reducer, userInitialContext);
  return (
    <div className={styles.app}>
      <UserContext.Provider
        value={{
          ...state,
          onLogin: () => {
            dispatch({
              type: 'login',
              payload: {
                logged: true
              }
            });
          },
          onLogout: () => {
            dispatch({
              type: 'logout',
              payload: {
                logged: false
              }
            });
          }
        }}>
        <ResourcesContext.Provider value={resources}>
          <Router>
            <Switch>
              <Route exact path="/" component={Login} />
              <PrivateRoute exact path={routes.DATAFLOW_TASKS} component={DataFlowTasks} />
              <PrivateRoute path={routes.REPORTING_DATAFLOW} component={ReportingDataFlow} />
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
