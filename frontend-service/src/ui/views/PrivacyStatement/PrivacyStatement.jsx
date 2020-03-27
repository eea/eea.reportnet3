import React, { useContext, useEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import styles from './Settings.module.scss';

import { routes } from 'ui/routes';
import { MainLayout } from 'ui/views/_components/Layout';
import { Title } from '../_components/Title/Title';
import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { UserDesignOptions } from './_components/UserDesignOptions';
import { UserCard } from './_components/UserCard';
import { UserConfiguration } from './_components/UserConfiguration';

const PrivacyStatement = withRouter(({ history }) => {
  const breadCrumbContext = useContext(BreadCrumbContext);
  const leftSideBarContext = useContext(LeftSideBarContext);
  const resources = useContext(ResourcesContext);

  useEffect(() => {
    breadCrumbContext.add([
      {
        label: '',
        icon: 'home',
        href: getUrl(routes.DATAFLOWS),
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      {
        label: resources.messages['privacyStatement'],
        icon: 'info',
        href: getUrl(routes.PRIVACY_STATEMENT),
        command: () => history.push(getUrl(routes.PRIVACY_STATEMENT))
      }
    ]);
  }, []);

  const toggleUserOptions = () => {
    return (
      <>
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

  return layout(
    <div>
      <div className="rep-row">
        <div className={` rep-col-12 rep-col-sm-12`}>
          <Title
            title={resources.messages['userSettingsTitle']}
            icon="user-profile"
            iconSize="4rem"
            subtitle={resources.messages['userSettingsSubtitle']}
          />
        </div>
      </div>

      <div className="rep-row">
        <div className={styles.sectionMainContent}>{toggleUserOptions()}</div>
      </div>
    </div>
  );
});

export { PrivacyStatement };
