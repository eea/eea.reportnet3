import React from 'react';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';

import styles from './App.module.css';

import { routes } from 'ui/routes';

import { DataCollection } from 'ui/views/DataCollection';
import { Dataflow } from 'ui/views/Dataflow';
import { DataflowDashboards } from 'ui/views/DataflowDashboards/DataflowDashboards';
import { DataflowHelp } from 'ui/views/DataflowHelp/DataflowHelp';
import { Dataflows } from 'ui/views/Dataflows';
import { Dataset } from 'ui/views/Dataset';
import { DatasetDesigner } from 'ui/views/DatasetDesigner/DatasetDesigner';
import { EUDataset } from 'ui/views/EUDataset';
import { EULogin } from 'ui/views/Login/EULogin';
import { Notifications } from 'ui/views/_components/Notifications';
import { PrivacyStatement } from 'ui/views/PrivacyStatement';
import { PrivateRoute } from 'ui/views/_components/PrivateRoute';
import { PublicFrontpage } from 'ui/views/PublicFrontpage';
import { ReportnetLogin } from 'ui/views/Login/ReportnetLogin';
import { Settings } from 'ui/views/Settings';

import { BreadCrumbProvider } from 'ui/views/_functions/Providers/BreadCrumbProvider';
import { DialogProvider } from 'ui/views/_functions/Providers/DialogProvider';
import { LeftSideBarProvider } from 'ui/views/_functions/Providers/LeftSideBarProvider';
import { LoadingProvider } from 'ui/views/_functions/Providers/LoadingProvider';
import { NotificationProvider } from 'ui/views/_functions/Providers/NotificationProvider';
import { ResourcesProvider } from 'ui/views/_functions/Providers/ResourcesProvider';
import { ThemeProvider } from 'ui/views/_functions/Providers/ThemeProvider';
import { UserProvider } from 'ui/views/_functions/Providers/UserProvider';
import { ValidationProvider } from 'ui/views/_functions/Providers/ValidationProvider';

const App = () => {
  return (
    <div className={styles.app}>
      <ResourcesProvider>
        <DialogProvider>
          <NotificationProvider>
            <UserProvider>
              <ThemeProvider>
                <ValidationProvider>
                  <LeftSideBarProvider>
                    <LoadingProvider>
                      <BreadCrumbProvider>
                        <Notifications />
                        <Router>
                          <Switch>
                            <Route exact path={routes.ACCESS_POINT} component={PublicFrontpage} />
                            <Route exact path={routes.LOGIN} component={ReportnetLogin} />
                            <Route exact path={routes.EULOGIN} component={EULogin} />
                            <PrivateRoute exact path={routes.DASHBOARDS} component={DataflowDashboards} />
                            <PrivateRoute exact path={routes.DATA_COLLECTION} component={DataCollection} />
                            <PrivateRoute exact path={routes.DATAFLOW_REPRESENTATIVE} component={Dataflow} />
                            <PrivateRoute exact path={routes.DATAFLOW} component={Dataflow} />
                            <PrivateRoute exact path={routes.DATAFLOWS} component={Dataflows} />
                            <PrivateRoute exact path={routes.DATASET_SCHEMA} component={DatasetDesigner} />
                            <PrivateRoute exact path={routes.DATASET} component={Dataset} />
                            <PrivateRoute exact path={routes.DOCUMENTS} component={DataflowHelp} />
                            <PrivateRoute exact path={routes.EU_DATASET} component={EUDataset} />
                            <PrivateRoute exact path={routes.PRIVACY_STATEMENT} component={PrivacyStatement} />
                            <PrivateRoute exact path={routes.SETTINGS} component={Settings} />
                          </Switch>
                        </Router>
                      </BreadCrumbProvider>
                    </LoadingProvider>
                  </LeftSideBarProvider>
                </ValidationProvider>
              </ThemeProvider>
            </UserProvider>
          </NotificationProvider>
        </DialogProvider>
      </ResourcesProvider>
    </div>
  );
};

export default App;
