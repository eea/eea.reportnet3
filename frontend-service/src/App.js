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
import { Feedback } from 'ui/views/Feedback';
import { Notifications } from 'ui/views/_components/Notifications';
import { PrivacyStatement } from 'ui/views/PrivacyStatement';
import { PrivateRoute } from 'ui/views/_components/PrivateRoute';
import { PublicCountries } from 'ui/views/PublicCountries';
import { PublicCountryInformation } from 'ui/views/PublicCountryInformation';
import { PublicDataflowInformation } from 'ui/views/PublicDataflowInformation';
import { PublicDataflows } from 'ui/views/PublicDataflows';
import { PublicFrontpage } from 'ui/views/PublicFrontpage';
import { ReportnetLogin } from 'ui/views/Login/ReportnetLogin';
import { ScrollToTop } from 'ui/views/_components/ScrollToTop';
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
                          <ScrollToTop>
                            <Switch>
                              <Route exact path={routes.PUBLIC_COUNTRIES} component={PublicCountries} />
                              <Route
                                exact
                                path={[routes.PUBLIC_COUNTRY_INFORMATION, routes.PUBLIC_COUNTRY_INFORMATION_ID]}
                                component={PublicCountryInformation}
                              />
                              <Route exact path={routes.PUBLIC_DATAFLOWS} component={PublicDataflows} />
                              <Route
                                exact
                                path={[routes.PUBLIC_DATAFLOW_INFORMATION, routes.PUBLIC_DATAFLOW_INFORMATION_ID]}
                                component={PublicDataflowInformation}
                              />
                              <Route exact path={routes.ACCESS_POINT} component={PublicFrontpage} />
                              <Route exact path={routes.LOGIN} component={ReportnetLogin} />
                              <Route exact path={routes.EULOGIN} component={EULogin} />
                              <PrivateRoute exact path={routes.DATAFLOW_FEEDBACK} component={Feedback} />
                              <PrivateRoute exact path={routes.DATAFLOW_FEEDBACK_CUSTODIAN} component={Feedback} />
                              <PrivateRoute exact path={routes.DASHBOARDS} component={DataflowDashboards} />
                              <PrivateRoute exact path={routes.DATA_COLLECTION} component={DataCollection} />
                              <PrivateRoute exact path={routes.DATAFLOW_REPRESENTATIVE} component={Dataflow} />
                              <PrivateRoute exact path={[routes.DATAFLOW, routes.DATAFLOWS_ID]} component={Dataflow} />
                              <PrivateRoute exact path={routes.DATAFLOWS} component={Dataflows} />
                              <PrivateRoute exact path={routes.DATAFLOWS_ERROR} component={Dataflows} />
                              <PrivateRoute exact path={routes.DATASET_SCHEMA} component={DatasetDesigner} />
                              <PrivateRoute exact path={routes.DATASET} component={Dataset} />
                              <PrivateRoute exact path={routes.DOCUMENTS} component={DataflowHelp} />
                              <PrivateRoute exact path={routes.EU_DATASET} component={EUDataset} />
                              <Route exact path={routes.PRIVACY_STATEMENT} component={PrivacyStatement} />
                              <PrivateRoute exact path={routes.SETTINGS} component={Settings} />
                            </Switch>
                          </ScrollToTop>
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
