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
import { RecoilRoot } from 'recoil';
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
      <RecoilRoot>
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
                                <Route exact path={routes.PUBLIC_COUNTRIES}>
                                  <PublicCountries />
                                </Route>
                                <Route exact path={routes.PUBLIC_COUNTRY_INFORMATION}>
                                  <PublicCountryInformation />
                                </Route>
                                <Route exact path={routes.PUBLIC_COUNTRY_INFORMATION_ID}>
                                  <PublicCountryInformation />
                                </Route>
                                <Route exact path={routes.PUBLIC_DATAFLOWS}>
                                  <PublicDataflows />
                                </Route>
                                <Route exact path={routes.PUBLIC_DATAFLOW_INFORMATION}>
                                  <PublicDataflowInformation />
                                </Route>
                                <Route exact path={routes.PUBLIC_DATAFLOW_INFORMATION_ID}>
                                  <PublicDataflowInformation />
                                </Route>
                                <Route exact path={routes.ACCESS_POINT}>
                                  <PublicFrontpage />
                                </Route>
                                <Route exact path={routes.LOGIN}>
                                  <ReportnetLogin />
                                </Route>
                                <Route exact path={routes.EULOGIN}>
                                  <EULogin />
                                </Route>
                                <Route exact path={routes.DATAFLOW_FEEDBACK}>
                                  <PrivateRoute component={Feedback} />
                                </Route>
                                <Route exact path={routes.DATAFLOW_FEEDBACK_CUSTODIAN}>
                                  <PrivateRoute component={Feedback} />
                                </Route>
                                <Route exact path={routes.DASHBOARDS}>
                                  <PrivateRoute component={DataflowDashboards} />
                                </Route>
                                <Route exact path={routes.DATA_COLLECTION}>
                                  <PrivateRoute component={DataCollection} />
                                </Route>
                                <Route exact path={routes.DATAFLOW_REPRESENTATIVE}>
                                  <PrivateRoute component={Dataflow} />
                                </Route>
                                <Route exact path={routes.DATAFLOW}>
                                  <PrivateRoute component={Dataflow} />
                                </Route>
                                <Route exact path={routes.DATAFLOWS_ID}>
                                  <PrivateRoute component={Dataflow} />
                                </Route>
                                <Route exact path={routes.DATAFLOWS}>
                                  <PrivateRoute component={Dataflows} />
                                </Route>
                                <Route exact path={routes.DATAFLOWS_ERROR}>
                                  <PrivateRoute component={Dataflows} />
                                </Route>
                                <Route exact path={routes.DATASET_SCHEMA}>
                                  <PrivateRoute component={DatasetDesigner} />
                                </Route>
                                <Route exact path={routes.REFERENCE_DATASET_SCHEMA}>
                                  <PrivateRoute
                                    component={DatasetDesigner}
                                    componentProps={{ isReferenceDataset: true }}
                                  />
                                </Route>
                                <Route exact path={routes.DATASET}>
                                  <PrivateRoute component={Dataset} />
                                </Route>
                                <Route exact path={routes.DOCUMENTS}>
                                  <PrivateRoute component={DataflowHelp} />
                                </Route>
                                <Route exact path={routes.EU_DATASET}>
                                  <PrivateRoute component={EUDataset} />
                                </Route>
                                <Route exact path={routes.PRIVACY_STATEMENT}>
                                  <PrivacyStatement />
                                </Route>
                                <Route exact path={routes.SETTINGS}>
                                  <PrivateRoute component={Settings} />
                                </Route>
                                <Route exact path={routes.REFERENCE_DATAFLOW}>
                                  <PrivateRoute component={ReferenceDataflow} />
                                </Route>
                                <Route exact path={routes.REFERENCE_DATASET}>
                                  <PrivateRoute component={Dataset} componentProps={{ isReferenceDataset: true }} />
                                </Route>
                                <Route exact path={routes.ACCESS_POINT_ERROR}>
                                  <PublicFrontpage />
                                </Route>
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
      </RecoilRoot>
    </div>
  );
};

export default App;
