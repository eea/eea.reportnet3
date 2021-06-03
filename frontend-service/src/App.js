import { BrowserRouter as Router, Redirect, Route, Switch } from 'react-router-dom';

import styles from './App.module.css';

import { routes } from 'ui/routes';

import { DataCollection } from 'ui/views/DataCollection';
import { Dataflow } from 'ui/views/Dataflow';
import { ReferenceDataflow } from 'ui/views/ReferenceDataflow';
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
import { PublicRoute } from 'ui/views/_components/PublicRoute';
import { ReportnetLogin } from 'ui/views/Login/ReportnetLogin';
import { ScrollToTop } from 'ui/views/_components/ScrollToTop';
import { Settings } from 'ui/views/Settings';
import { ErrorPage } from 'ui/views/ErrorPage';

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
                              <PrivateRoute component={Dataset} exact path={routes.DATASET} />
                              <PrivateRoute component={DataflowHelp} exact path={routes.DOCUMENTS} />
                              <PrivateRoute component={EUDataset} exact path={routes.EU_DATASET} />
                              <Route component={PrivacyStatement} exact path={routes.PRIVACY_STATEMENT} />
                              <PrivateRoute component={Settings} exact path={routes.SETTINGS} />
                              <PrivateRoute component={ErrorPage} exact path={routes.ERROR_PAGE} />
                              <PrivateRoute component={ReferenceDataflow} exact path={routes.REFERENCE_DATAFLOW} />
                              <Route>
                                <Redirect to={'/dataflows/error/notFound'} />
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
