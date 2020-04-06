import React, { useContext, useEffect, useReducer, useState } from 'react';
import { withRouter } from 'react-router-dom';
import { UserService } from 'core/services/User';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import styles from './Settings.module.scss';

import { Spinner } from 'ui/views/_components/Spinner';
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

const initialState = {
  isVisibleUserDesignOptions: true,
  isVisibleUserSettingsOptions: false
};

const reducer = (state, { type, payload }) => {
  switch (type) {
    case 'VISIBLE_USER_DESIGN_OPTIONS':
      return {
        ...state,
        isVisibleUserDesignOptions: true,
        isVisibleUserSettingsOptions: false
      };
    case 'VISIBLE_USER_SETTINGS_OPTIONS':
      return {
        ...state,
        isVisibleUserSettingsOptions: true,
        isVisibleUserDesignOptions: false
      };
    default:
      return state;
  }
};

const Settings = withRouter(({ history }) => {
  const [userConfiguration, setUserConfiguration] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [visibleUserSectionState, visibleUserSectionDispatch] = useReducer(reducer, initialState);
  
  const userContext = useContext(UserContext);
  const breadCrumbContext = useContext(BreadCrumbContext);
  const leftSideBarContext = useContext(LeftSideBarContext);
  const resources = useContext(ResourcesContext);
  const initUserSettingsSection = () => {
    visibleUserSectionDispatch({ type: 'VISIBLE_USER_DESIGN_OPTIONS' });
  };

  const getUserConfiguration = async () => {
    try {
      console.log('User Configuration: ', userConfiguration);
      console.log('User Context', userContext);
      const userConfiguration = await UserService.getConfiguration();
      setUserConfiguration(userConfiguration);
      userContext.dateFormat(userConfiguration.dateFormat);
      userContext.defaultRowSelected(parseInt(userConfiguration.defaultRowsNumber));
      userContext.onToggleLogoutConfirm(userConfiguration.defaultLogoutConfirmation);      
    } catch (error) {
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    getUserConfiguration();
    console.log('userConfiguration', userConfiguration);
  }, []);


  useEffect(() => {
    userContext.defaultRowSelected(parseInt(userConfiguration.defaultRowSelected));
  }, []);

  useEffect(() => {
    document.querySelectorAll('.userSettingsBtn').forEach(btn => {
      btn.addEventListener('click', initUserSettingsSection);
    });
    return () => {
      document.querySelectorAll('.userSettingsBtn').forEach(btn => {
        btn.removeEventListener('click', initUserSettingsSection);
      });
    };
  }, []);

  useEffect(() => {
    breadCrumbContext.add([
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
  }, []);

  useEffect(() => {
    leftSideBarContext.addModels([
      {
        icon: 'palette',
        label: 'userDesignOptions',
        onClick: () => {
          visibleUserSectionDispatch({
            type: 'VISIBLE_USER_DESIGN_OPTIONS'
          });
        },
        title: 'User Design Options'
      },
      {
        icon: 'userConfig',
        label: 'userConfigurationOptions',
        onClick: () => {
          visibleUserSectionDispatch({
            type: 'VISIBLE_USER_SETTINGS_OPTIONS'
          });
        },
        title: 'User Configuration Options'
      },
      {
        icon: 'info',
        label: 'PRIVACY',
        onClick: e => {
          e.preventDefault();
          history.push(getUrl(routes['PRIVACY_STATEMENT']));
        },
        title: 'User Configuration Options'
      }
    ]);
  }, []);

  const toggleUserOptions = () => {
    return (
      <>
        {visibleUserSectionState.isVisibleUserDesignOptions && <UserDesignOptions />}
        {visibleUserSectionState.isVisibleUserSettingsOptions &&  <UserConfiguration />}
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

  return (
    <>
      {isLoading ? <Spinner className={styles.positioning} /> : userConfigurations()}
    </>
  );
});

export { Settings };
