import React, { useContext } from 'react';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';

import styles from './App.module.css';
import { routes } from 'ui/routes';

import { AccessPoint } from 'ui/views/Login/AccessPoint';
import { Codelists } from 'ui/views/Codelists';
import { DataCollection } from 'ui/views/DataCollection';
import { Dataflow } from 'ui/views/Dataflow';
import { DataflowDashboards } from 'ui/views/DataflowDashboards/DataflowDashboards';
import { DataflowHelp } from 'ui/views/DataflowHelp/DataflowHelp';
import { Dataflows } from 'ui/views/Dataflows';
import { Dataset } from 'ui/views/Dataset';
import { DatasetDesigner } from 'ui/views/DatasetDesigner/DatasetDesigner';
import { EULogin } from 'ui/views/Login/EULogin';
import { LoadingProvider } from 'ui/views/_functions/Providers/LoadingProvider';
import { NotificationProvider } from 'ui/views/_functions/Providers/NotificationProvider';
import { Notifications } from 'ui/views/_components/Notifications';
import { PrivateRoute } from 'ui/views/_components/PrivateRoute';
import { ReportnetLogin } from 'ui/views/Login/ReportnetLogin';
import { Representative } from 'ui/views/Representative/Representative';
import { ResourcesProvider } from 'ui/views/_functions/Providers/ResourcesProvider';
import { UserProvider } from 'ui/views/_functions/Providers/UserProvider';

const App = () => {
  return (
    <div className={styles.app}>
      <ResourcesProvider>
        <UserProvider>
          <NotificationProvider>
            <LoadingProvider>
              <Notifications />
              <Router>
                <Switch>
                  <Route
                    exact
                    path="/"
                    component={window.env.REACT_APP_EULOGIN == 'true' ? AccessPoint : ReportnetLogin}
                  />
                  <Route exact path={routes.EULOGIN} component={EULogin} />
                  <PrivateRoute exact path={routes.CODELISTS} component={Codelists} />
                  <PrivateRoute exact path={routes.DATA_COLLECTION} component={DataCollection} />                  
                  <PrivateRoute exact path={routes.DATASET_SCHEMA} component={DatasetDesigner} />
                  <PrivateRoute exact path={routes.DASHBOARDS} component={DataflowDashboards} />
                  <PrivateRoute exact path={routes.DATAFLOW} component={Dataflow} />
                  <PrivateRoute exact path={routes.DATAFLOWS} component={Dataflows} />
                  <PrivateRoute exact path={routes.REPRESENTATIVE} component={Representative} />
                  <PrivateRoute exact path={routes.DATASET} component={Dataset} />
                  <PrivateRoute exact path={routes.DOCUMENTS} component={DataflowHelp} />
                </Switch>
              </Router>
            </LoadingProvider>
          </NotificationProvider>
        </UserProvider>
      </ResourcesProvider>
    </div>
  );
};

export default App;
