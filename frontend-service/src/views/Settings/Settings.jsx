import { Fragment, useContext, useEffect } from 'react';
import { withRouter } from 'react-router-dom';

import { SettingsHelpConfig } from 'conf/help/settings';

import styles from './Settings.module.scss';

import { MainLayout } from 'views/_components/Layout';
import { Title } from '../_components/Title/Title';
import { UserCard } from './_components/UserCard';
import { UserConfiguration } from './_components/UserConfiguration';

import { LeftSideBarContext } from 'views/_functions/Contexts/LeftSideBarContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { useBreadCrumbs } from 'views/_functions/Hooks/useBreadCrumbs';
import { CurrentPage } from 'views/_functions/Utils';

const Settings = withRouter(({ history }) => {
  const leftSideBarContext = useContext(LeftSideBarContext);
  const resourcesContext = useContext(ResourcesContext);

  useBreadCrumbs({ currentPage: CurrentPage.USER_SETTINGS, history });

  useEffect(() => {
    leftSideBarContext.addModels([]);
    leftSideBarContext.addHelpSteps(SettingsHelpConfig, 'settingsHelp');
  }, []);

  const renderUserOptions = () => {
    return (
      <Fragment>
        <div className={`${styles.userConfiguration} settings-change-settings-help-step`}>
          <UserConfiguration />
        </div>
        <div className={`${styles.userCard} settings-change-avatar-help-step`}>
          <UserCard />
        </div>
      </Fragment>
    );
  };

  const layout = children => {
    return (
      <MainLayout leftSideBarConfig={{ buttons: [] }}>
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
              subtitle={resourcesContext.messages['userSettingsSubtitle']}
              title={resourcesContext.messages['userSettingsTitle']}
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
