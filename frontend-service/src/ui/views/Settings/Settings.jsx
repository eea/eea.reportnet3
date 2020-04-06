import React, { useContext, useEffect, useReducer, useState } from 'react';
import { withRouter } from 'react-router-dom';
import { UserService } from 'core/services/User';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

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
  const userContext = useContext(UserContext);
  const breadCrumbContext = useContext(BreadCrumbContext);
  const leftSideBarContext = useContext(LeftSideBarContext);
  const resources = useContext(ResourcesContext);
  const [visibleUserSectionState, visibleUserSectionDispatch] = useReducer(reducer, initialState);
  const initUserSettingsSection = () => {
    visibleUserSectionDispatch({ type: 'VISIBLE_USER_DESIGN_OPTIONS' });
  };
  const [attributes, setAttributes] = useState({});

  useEffect(() => {
    setAttributes({
      defaultRowSelected: [userContext.userProps.defaultRowSelected],
      defaultVisualTheme: [`${userContext.userProps.defaultVisualTheme}`],
      showLogoutConfirmation: [`${userContext.userProps.showLogoutConfirmation}`],
      dateFormat: [`${userContext.userProps.dateFormat}`]
    });
  }, [userContext]);

  const updateUserConfiguration = async userConfiguration => {
    try {
      const updateUserConfiguration = await UserService.updateAttributes(userConfiguration);
      console.log('updateUserConfiguration', updateUserConfiguration);
    } catch (error) {
      console.error(error);
    }
  };

  const getUserConfiguration = async () => {
    try {
      console.log('awat');
      const userConfiguration = await UserService.getConfiguration();
      console.log(userConfiguration);
      userContext.dateFormat(userConfiguration.dateFormat);
      userContext.defaultRowSelected(parseInt(userConfiguration.defaultRowsNumber));
      userContext.onToggleLogoutConfirm(userConfiguration.defaultLogoutConfirmation);
      setUserConfiguration(userConfiguration);
      console.log('userC', userConfiguration);
    } catch (error) {
      console.error(error);
    }
  };

  useEffect(() => {
    getUserConfiguration();
  }, []);

  /////////////////////////////////////////////////
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

  //////////////////////////////////////////////////////////////////////////////////////////////////

  //////////////////////////////////////////////////////////////////////////////////////////////////
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

export { Settings };
