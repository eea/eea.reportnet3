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
import { UserConfigurationOptions } from './_components/UserConfigurationOptions';

const initialState = {
  isVisibleUserDesignOptions: true,
  isVisibleUserSettingsOptions: false
};

const reducer = (state, { type, payload }) => {
  switch (type) {
    case 'VISIBLE_USER_DESIGN_OPTIONS':
      return {
        ...state,
        isVisibleUserDesignOptions: payload.isVisibleUserDesignOptions,
        isVisibleUserSettingsOptions: payload.isVisibleUserSettingsOptions
      };
    case 'VISIBLE_USER_SETTINGS_OPTIONS':
      return {
        ...state,
        isVisibleUserSettingsOptions: payload.isVisibleUserSettingsOptions,
        isVisibleUserDesignOptions: payload.isVisibleUserDesignOptions
      };
    default:
      return state;
  }
};

const Settings = withRouter(({ history }) => {
  const breadCrumbContext = useContext(BreadCrumbContext);
  const leftSideBarContext = useContext(LeftSideBarContext);
  const resources = useContext(ResourcesContext);

  const [visibleUserSectionState, visibleUserSectionDispatch] = useReducer(reducer, initialState);

  useEffect(() => {
    breadCrumbContext.add([
      {
        label: '',
        icon: 'home',
        href: getUrl(routes.DATAFLOWS),
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      {
        label: resources.messages['settingsUser'],
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
            type: 'VISIBLE_USER_DESIGN_OPTIONS',
            payload: {
              isVisibleUserDesignOptions: true,
              isVisibleUserSettingsOptions: false
            }
          });
        },
        title: 'User Design Options'
      },
      {
        icon: 'configs',
        label: 'userConfigurationOptions',
        onClick: () => {
          visibleUserSectionDispatch({
            type: 'VISIBLE_USER_SETTINGS_OPTIONS',
            payload: {
              isVisibleUserDesignOptions: false,
              isVisibleUserSettingsOptions: true
            }
          });
        },
        title: 'User Configuration Options'
      }
    ]);
  }, []);

  console.log(
    'UserDesignOptions ',
    visibleUserSectionState.isVisibleUserDesignOptions,
    ' UserConfigurationOptions ',
    visibleUserSectionState.isVisibleUserSettingsOptions
  );

  const toggleUserOptions = () => {
    return (
      <>
        {visibleUserSectionState.isVisibleUserDesignOptions && <UserDesignOptions />}

        {visibleUserSectionState.isVisibleUserSettingsOptions && <UserConfigurationOptions />}

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
          <Title title={resources.messages['settingsUser']} icon="user-profile" iconSize="4rem" />
        </div>
      </div>

      <div className="rep-row">
        <div className={styles.pageMainContent}>{toggleUserOptions()}</div>
      </div>
    </div>
  );
});

export { Settings };
