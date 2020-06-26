import React, { useContext, useEffect } from 'react';
import { withRouter } from 'react-router-dom';
import { getUrl } from 'core/infrastructure/CoreUtils';

import styles from './Settings.module.scss';
import { routes } from 'ui/routes';

import { MainLayout } from 'ui/views/_components/Layout';
import { Title } from '../_components/Title/Title';
import { UserCard } from './_components/UserCard';
import { UserConfiguration } from './_components/UserConfiguration';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const Settings = withRouter(({ history }) => {
  const breadCrumbContext = useContext(BreadCrumbContext);

  const leftSideBarContext = useContext(LeftSideBarContext);
  const resources = useContext(ResourcesContext);

  useEffect(() => {
    breadCrumbContext.add([
      {
        label: resources.messages['homeBreadcrumb'],
        href: getUrl(routes.DATAFLOWS),
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      {
        label: '',
        icon: 'home',
        href: getUrl(routes.DATAFLOWS),
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      {
        label: resources.messages['userSettingsBreadcrumbs'],
        icon: 'user-profile',
        href: getUrl(routes.SETTINGS),
        command: () => history.push(getUrl(routes.SETTINGS))
      }
    ]);
    leftSideBarContext.addModels([]);
  }, []);

  const renderUserOptions = () => {
    return (
      <>
        <UserConfiguration />
        <UserCard />
      </>
    );
  };

  const layout = children => {
    return (
      <MainLayout
        leftSideBarConfig={{
          buttons: []
        }}>
        <div className>
          <div className="rep-container">{children}</div>
        </div>
      </MainLayout>
    );
  };

  const userConfigurations = () =>
    layout(
      <div className={styles.settingsContainer}>
        <div className="rep-row">
          <div className={` rep-col-12 rep-col-sm-12`}>
            <Title
              title={resources.messages['userSettingsTitle']}
              icon="userConfig"
              iconSize="4rem"
              subtitle={resources.messages['userSettingsSubtitle']}
            />
          </div>
        </div>
        <div className="rep-row">
          <div className={styles.sectionMainContent}>{renderUserOptions()}</div>
        </div>
      </div>
    );

  return userConfigurations();
});

export { Settings };
