import { BrowserRouter as Router, Navigate, Route, Routes } from 'react-router-dom';

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
          <NotificationProvider>
            <UserProvider>
              <ThemeProvider>
                <ValidationProvider>
                  <LeftSideBarProvider>
                    <LoadingProvider>
                      <BreadCrumbProvider>
                        <Notifications />
                        <Router>
                          <ScrollToTop />
                          <Routes>
                            <Route element={<PublicCountries />} exact path={routes.PUBLIC_COUNTRIES} />
                            <Route
                              element={<PublicCountryInformation />}
                              exact
                              path={routes.PUBLIC_COUNTRY_INFORMATION}
                            />
                            <Route
                              element={<PublicCountryInformation />}
                              exact
                              path={routes.PUBLIC_COUNTRY_INFORMATION_ID}
                            />
                            <Route element={<PublicDataflows />} exact path={routes.PUBLIC_DATAFLOWS} />
                            <Route
                              element={<PublicDataflowInformation />}
                              exact
                              path={routes.PUBLIC_DATAFLOW_INFORMATION}
                            />
                            <Route
                              element={<PublicDataflowInformation />}
                              exact
                              path={routes.PUBLIC_DATAFLOW_INFORMATION_ID}
                            />
                            <Route element={<PublicFrontpage />} exact path={routes.ACCESS_POINT} />
                            <Route element={<ReportnetLogin />} exact path={routes.LOGIN} />
                            <Route element={<EULogin />} exact path={routes.EULOGIN} />
                            <Route
                              element={<PrivateRoute component={Feedback} />}
                              exact
                              path={routes.DATAFLOW_FEEDBACK}
                            />
                            <Route
                              element={<PrivateRoute component={Feedback} />}
                              exact
                              path={routes.DATAFLOW_FEEDBACK_CUSTODIAN}
                            />
                            <Route
                              element={<PrivateRoute component={DataflowDashboards} />}
                              exact
                              path={routes.DASHBOARDS}
                            />
                            <Route
                              element={<PrivateRoute component={DataCollection} />}
                              exact
                              path={routes.DATA_COLLECTION}
                            />
                            <Route
                              element={<PrivateRoute component={Dataflow} />}
                              exact
                              path={routes.DATAFLOW_REPRESENTATIVE}
                            />
                            <Route element={<PrivateRoute component={Dataflow} />} exact path={routes.DATAFLOW} />
                            <Route element={<PrivateRoute component={Dataflow} />} exact path={routes.DATAFLOWS_ID} />
                            <Route element={<PrivateRoute component={Dataflows} />} exact path={routes.DATAFLOWS} />
                            <Route
                              element={<PrivateRoute component={Dataflows} />}
                              exact
                              path={routes.DATAFLOWS_ERROR}
                            />
                            <Route
                              element={<PrivateRoute component={DatasetDesigner} />}
                              exact
                              path={routes.DATASET_SCHEMA}
                            />
                            <Route
                              element={
                                <PrivateRoute
                                  component={DatasetDesigner}
                                  componentProps={{ isReferenceDataset: true }}
                                />
                              }
                              exact
                              path={routes.REFERENCE_DATASET_SCHEMA}
                            />
                            <Route element={<PrivateRoute component={Dataset} />} exact path={routes.DATASET} />
                            <Route element={<PrivateRoute component={DataflowHelp} />} exact path={routes.DOCUMENTS} />
                            <Route element={<PrivateRoute component={EUDataset} />} exact path={routes.EU_DATASET} />
                            <Route element={<PrivacyStatement />} exact path={routes.PRIVACY_STATEMENT} />
                            <Route element={<PrivateRoute component={Settings} />} exact path={routes.SETTINGS} />
                            <Route
                              element={<PrivateRoute component={ReferenceDataflow} />}
                              exact
                              path={routes.REFERENCE_DATAFLOW}
                            />
                            <Route
                              element={
                                <PrivateRoute component={Dataset} componentProps={{ isReferenceDataset: true }} />
                              }
                              exact
                              path={routes.REFERENCE_DATASET}
                            />
                            <Route element={<PublicFrontpage />} exact path={routes.ACCESS_POINT_ERROR} />
                            <Route element={<Navigate to={'/error/notFound'} />} path="*" />
                          </Routes>
                        </Router>
                      </BreadCrumbProvider>
                    </LoadingProvider>
                  </LeftSideBarProvider>
                </ValidationProvider>
              </ThemeProvider>
            </UserProvider>
          </NotificationProvider>
        </ResourcesProvider>
      </RecoilRoot>
    </div>
  );
};

export default App;
