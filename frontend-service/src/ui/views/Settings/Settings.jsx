import { useContext, useEffect } from 'react';
import { withRouter } from 'react-router-dom';

import { SettingsHelpConfig } from 'conf/help/settings';

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

  useBreadCrumbs({ currentPage: CurrentPage.USER_SETTINGS, history });

  useEffect(() => {
    leftSideBarContext.addModels([]);
    leftSideBarContext.addHelpSteps(SettingsHelpConfig, 'settingsHelp');
  }, []);

  const renderUserOptions = () => {
    return (
      <>
        <div className={`${styles.userConfiguration} settings-change-settings-help-step`}>
          <UserConfiguration />
        </div>
        <div className={`${styles.userCard} settings-change-avatar-help-step`}>
          <UserCard />
        </div>
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
              icon="userConfig"
              iconSize="4rem"
              subtitle={resources.messages['userSettingsSubtitle']}
              title={resources.messages['userSettingsTitle']}
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
