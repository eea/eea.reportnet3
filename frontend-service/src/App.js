import { BrowserRouter as Router, Redirect, Route, Switch } from 'react-router-dom';

import styles from './App.module.css';

import { routes } from 'conf/routes';

import { DataCollection } from 'views/DataCollection';
import { Dataflow } from 'views/Dataflow';
import { ReferenceDataflow } from 'views/ReferenceDataflow';
import { DataflowDashboards } from 'views/DataflowDashboards/DataflowDashboards';
import { DataflowHelp } from 'views/DataflowHelp/DataflowHelp';
import { Dataflows } from 'views/Dataflows';
import { Dataset } from 'views/Dataset';
import { DatasetDesigner } from 'views/DatasetDesigner/DatasetDesigner';
import { EUDataset } from 'views/EUDataset';
import { EULogin } from 'views/Login/EULogin';
import { Feedback } from 'views/Feedback';
import { Notifications } from 'views/_components/Notifications';
import { PrivacyStatement } from 'views/PrivacyStatement';
import { PrivateRoute } from 'views/_components/PrivateRoute';
import { PublicCountries } from 'views/PublicCountries';
import { PublicCountryInformation } from 'views/PublicCountryInformation';
import { PublicDataflowInformation } from 'views/PublicDataflowInformation';
import { PublicDataflows } from 'views/PublicDataflows';
import { PublicFrontpage } from 'views/PublicFrontpage';
import { PublicRoute } from 'views/_components/PublicRoute';
import { ReportnetLogin } from 'views/Login/ReportnetLogin';
import { ScrollToTop } from 'views/_components/ScrollToTop';
import { Settings } from 'views/Settings';

import { BreadCrumbProvider } from 'views/_functions/Providers/BreadCrumbProvider';
import { DialogProvider } from 'views/_functions/Providers/DialogProvider';
import { LeftSideBarProvider } from 'views/_functions/Providers/LeftSideBarProvider';
import { LoadingProvider } from 'views/_functions/Providers/LoadingProvider';
import { NotificationProvider } from 'views/_functions/Providers/NotificationProvider';
import { ResourcesProvider } from 'views/_functions/Providers/ResourcesProvider';
import { ThemeProvider } from 'views/_functions/Providers/ThemeProvider';
import { UserProvider } from 'views/_functions/Providers/UserProvider';
import { ValidationProvider } from 'views/_functions/Providers/ValidationProvider';

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
                              <PublicRoute component={PublicCountries} exact path={routes.PUBLIC_COUNTRIES} />
                              <PublicRoute
                                component={PublicCountryInformation}
                                exact
                                path={routes.PUBLIC_COUNTRY_INFORMATION}
                              />
                              <PublicRoute
                                component={PublicCountryInformation}
                                exact
                                parentPath={routes.PUBLIC_COUNTRY_INFORMATION}
                                path={routes.PUBLIC_COUNTRY_INFORMATION_ID}
                              />
                              <PublicRoute component={PublicDataflows} exact path={routes.PUBLIC_DATAFLOWS} />
                              <PublicRoute
                                component={PublicDataflowInformation}
                                exact
                                path={routes.PUBLIC_DATAFLOW_INFORMATION}
                              />
                              <PublicRoute
                                component={PublicDataflowInformation}
                                exact
                                parentPath={routes.PUBLIC_DATAFLOW_INFORMATION}
                                path={routes.PUBLIC_DATAFLOW_INFORMATION_ID}
                              />
                              <Route component={PublicFrontpage} exact path={routes.ACCESS_POINT} />
                              <Route component={ReportnetLogin} exact path={routes.LOGIN} />
                              <Route component={EULogin} exact path={routes.EULOGIN} />
                              <PrivateRoute component={Feedback} exact path={routes.DATAFLOW_FEEDBACK} />
                              <PrivateRoute component={Feedback} exact path={routes.DATAFLOW_FEEDBACK_CUSTODIAN} />
                              <PrivateRoute component={DataflowDashboards} exact path={routes.DASHBOARDS} />
                              <PrivateRoute component={DataCollection} exact path={routes.DATA_COLLECTION} />
                              <PrivateRoute component={Dataflow} exact path={routes.DATAFLOW_REPRESENTATIVE} />
                              <PrivateRoute component={Dataflow} exact path={routes.DATAFLOW} />
                              <PrivateRoute
                                component={Dataflow}
                                exact
                                parentPath={routes.DATAFLOW}
                                path={routes.DATAFLOWS_ID}
                              />
                              <PrivateRoute component={Dataflows} exact path={routes.DATAFLOWS} />
                              <PrivateRoute component={Dataflows} exact path={routes.DATAFLOWS_ERROR} />
                              <PrivateRoute component={DatasetDesigner} exact path={routes.DATASET_SCHEMA} />
                              <PrivateRoute
                                component={DatasetDesigner}
                                componentProps={{ isReferenceDataset: true }}
                                exact
                                path={routes.REFERENCE_DATASET_SCHEMA}
                              />
                              <PrivateRoute component={Dataset} exact path={routes.DATASET} />
                              <PrivateRoute component={DataflowHelp} exact path={routes.DOCUMENTS} />
                              <PrivateRoute component={EUDataset} exact path={routes.EU_DATASET} />
                              <Route component={PrivacyStatement} exact path={routes.PRIVACY_STATEMENT} />
                              <PrivateRoute component={Settings} exact path={routes.SETTINGS} />
                              <PrivateRoute component={ReferenceDataflow} exact path={routes.REFERENCE_DATAFLOW} />
                              <PrivateRoute
                                component={Dataset}
                                componentProps={{ isReferenceDataset: true }}
                                exact
                                path={routes.REFERENCE_DATASET}
                              />
                              <Route component={PublicFrontpage} exact path={routes.ACCESS_POINT_ERROR} />
                              <Route>
                                <Redirect to={'/error/notFound'} />
                              </Route>
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
