import React, { useContext, useEffect } from 'react';
import { withRouter } from 'react-router-dom';

import styles from './Settings.module.scss';

import { MainLayout } from 'ui/views/_components/Layout';
import { Title } from '../_components/Title/Title';
import { UserCard } from './_components/UserCard';
import { UserConfiguration } from './_components/UserConfiguration';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';
import { CurrentPage } from 'ui/views/_functions/Utils';

const Settings = withRouter(({ history }) => {
  const leftSideBarContext = useContext(LeftSideBarContext);
  const resources = useContext(ResourcesContext);

  useBreadCrumbs(history, CurrentPage.USER_SETTINGS);

  useEffect(() => {
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
        <div>
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
