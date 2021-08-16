import { Fragment, useContext, useEffect, useRef, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { config } from 'conf';

import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import defaultAvatar from 'assets/images/avatars/defaultAvatar.png';
import logo from 'assets/images/logos/logo.png';
import styles from './Header.module.scss';
import ReportnetPublicLogo from 'assets/images/logos/reportnet_public_logo.svg';

import { AccessPointWebConfig } from 'conf/domain/model/AccessPoint/AccessPoint.web.config';

import { routes } from 'ui/routes';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'ui/views/_components/Checkbox';
import { EuHeader } from 'ui/views/_components/Layout/MainLayout/_components/EuHeader';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { UserService } from 'core/services/User';
import { InputSwitch } from 'ui/views/_components/InputSwitch';

import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

const Header = withRouter(({ history, onMainContentStyleChange = () => {}, isPublic = false }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);
  const themeContext = useContext(ThemeContext);

  const avatarImage = useRef();

  const [confirmvisible, setConfirmVisible] = useState(false);
  const [doNotRemember, setDoNotRemember] = useState(false);

  const [globanElementStyle, setGlobanElementStyle] = useState({
    marginTop: 0,
    transition: '0.5s'
  });
  const [euHeaderElementStyle, setEuHeaderElementStyle] = useState({
    marginTop: 0,
    transition: '0.5s'
  });
  const [headerElementStyle, setHeaderElementStyle] = useState({ transition: '0.5s' });

  useEffect(() => {
    window.onscroll = () => {
      const innerWidth = window.innerWidth;
      const currentScrollPos = window.pageYOffset;
      if (innerWidth > 768 && themeContext.headerCollapse) {
        if (currentScrollPos === 0) {
          setGlobanElementStyle({
            marginTop: '0',
            transition: '0.5s'
          });
          setEuHeaderElementStyle({
            marginTop: '0',
            transition: '0.5s'
          });
          setHeaderElementStyle({
            ...headerElementStyle,
            height: '180px'
          });
          onMainContentStyleChange({
            marginTop: '180px',
            transition: '0.5s'
          });
        } else {
          setGlobanElementStyle({
            marginTop: '-100px',
            transition: '0.5s'
          });
          setEuHeaderElementStyle({
            marginTop: '-16px',
            transition: '0.5s'
          });
          setHeaderElementStyle({
            ...headerElementStyle,
            height: '64px'
          });
          onMainContentStyleChange({
            marginTop: '64px',
            transition: '0.5s'
          });
        }
      }
    };
    if (!themeContext.headerCollapse) {
      setHeaderElementStyle({
        ...headerElementStyle,
        height: `${config.theme.baseHeaderHeight + config.theme.cookieConsentHeight}px`
      });
    } else {
      setHeaderElementStyle({ ...headerElementStyle, height: `${config.theme.baseHeaderHeight}px` });
    }
  }, [themeContext.headerCollapse]);

  useEffect(() => {
    if (!isEmpty(userContext.userProps.userImage) && userContext.userProps.userImage.join('') !== '') {
      onLoadImage();
    }
  }, [userContext.userProps.userImage]);

  const checkDoNotRemember = (
    <div style={{ float: 'left' }}>
      <Checkbox
        checked={doNotRemember}
        id="do_not_remember_checkbox"
        inputId="do_not_remember_checkbox"
        onChange={e => setDoNotRemember(e.checked)}
        role="checkbox"
      />
      <label
        onClick={() => setDoNotRemember(!doNotRemember)}
        style={{ cursor: 'pointer', fontWeight: 'bold', marginLeft: '3px' }}>
        {resources.messages['doNotAskAgain']}
      </label>
    </div>
  );

  const loadTitle = () => (
    <a
      className={styles.title}
      href={getUrl(routes.ACCESS_POINT)}
      onClick={e => {
        e.preventDefault();
        history.push(getUrl(routes.ACCESS_POINT));
      }}
      title={resources.messages['titleHeader']}>
      {isPublic ? (
        <img alt="Reportnet 3" className={styles.appLogo} height="50px" src={ReportnetPublicLogo} />
      ) : (
        <img alt="Reportnet 3" className={styles.appLogo} height="50px" src={logo} />
      )}
    </a>
  );

  const isLocalEnvironment = () => {
    let url = window.location.href;
    if (url.toString().includes('localhost')) {
      return true;
    }
    return false;
  };

  const localhostEnvironmentAlert = isLocalEnvironment() && (
    <div className={styles.localhostAlert}>
      <FontAwesomeIcon
        aria-labelledby={resources.messages['localhostAlert']}
        icon={AwesomeIcons('localhostAlert')}
        role="button"
        title={resources.messages['localhostAlert']}
      />
    </div>
  );

  const themeSwitcher = isLocalEnvironment() && !isPublic && (
    <InputSwitch
      aria-label={resources.messages['toggleDarkTheme']}
      checked={themeContext.currentTheme === 'dark'}
      onChange={e => {
        userContext.onToggleVisualTheme(e.value ? 'dark' : 'light');
        themeContext.onToggleTheme(e.value ? 'dark' : 'light');
      }}
      sliderCheckedClassName={styles.themeSwitcherInputSwitch}
      style={{ marginRight: '1rem' }}
      tooltip={
        themeContext.currentTheme === 'light'
          ? resources.messages['toggleDarkTheme']
          : resources.messages['toggleLightTheme']
      }
      tooltipOptions={{ position: 'bottom', className: styles.themeSwitcherTooltip }}
    />
  );

  const userLogout = async () => {
    if (doNotRemember) {
      userContext.onToggleLogoutConfirm(false);
      const inmUserProperties = { ...userContext.userProps };
      inmUserProperties.showLogoutConfirmation = false;
      try {
        await UserService.updateAttributes(inmUserProperties);
      } catch (error) {
        notificationContext.add({
          type: 'UPDATE_ATTRIBUTES_USER_SERVICE_ERROR'
        });
      }
    }
    userContext.socket.deactivate();
    try {
      await UserService.logout();
    } catch (error) {
      notificationContext.add({
        type: 'USER_LOGOUT_ERROR'
      });
    } finally {
      userContext.onLogout();
    }
  };

  const userProfileSettingsButton = (
    <a
      className="userSettingsBtn"
      href={getUrl(routes.SETTINGS)}
      onClick={async e => {
        e.preventDefault();
        history.push(getUrl(routes.SETTINGS));
      }}
      title="User profile details">
      <img
        alt="User avatar"
        className={styles.userAvatar}
        icon={
          <FontAwesomeIcon aria-hidden={false} className={styles.userDataIcon} icon={AwesomeIcons('user-profile')} />
        }
        ref={avatarImage}
        src={isEmpty(userContext.userProps.userImage) ? defaultAvatar : null}
      />
      <span>
        {!isUndefined(userContext.email) &&
        !isUndefined(userContext.lastName) &&
        userContext.firstName !== '' &&
        userContext.lastName !== ''
          ? `${userContext.firstName} ${userContext.lastName}`
          : !isUndefined(userContext.email) && userContext.email !== ''
          ? userContext.email
          : userContext.preferredUsername}
      </span>
    </a>
  );

  const logout = (
    <div
      className={styles.logoutWrapper}
      onClick={async e => {
        e.preventDefault();
        userContext.userProps.showLogoutConfirmation ? setConfirmVisible(true) : userLogout();
      }}>
      <FontAwesomeIcon
        aria-hidden={false}
        aria-label="Logout"
        className={styles.logoutButton}
        icon={AwesomeIcons('logout')}
        role="presentation"
      />
    </div>
  );

  const loadUser = () => (
    <Fragment>
      <div className={styles.userWrapper}>
        {themeSwitcher}
        {localhostEnvironmentAlert}
        {userProfileSettingsButton}
      </div>

      <div className={styles.logoutBtnContainer}>{logout}</div>
    </Fragment>
  );

  const loadLogin = () => (
    <div className={styles.loginWrapper}>
      <Button
        className="p-button-primary"
        label={resources.messages.login}
        onClick={() => {
          if (window.env.REACT_APP_EULOGIN.toString() === 'true') {
            window.location.href = AccessPointWebConfig.euloginUrl;
          } else {
            history.push(getUrl(routes.LOGIN));
          }
        }}
        style={{ padding: '0.25rem 2rem', borderRadius: '25px', fontWeight: 'bold' }}></Button>
    </div>
  );

  const onLoadImage = () => {
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    const { current } = avatarImage;
    if (!isUndefined(current)) {
      current.onload = function () {
        ctx.drawImage(current, 0, 0);
      };
      current.src = userContext.userProps.userImage.join('');
    }
  };

  return (
    <div className={`${styles.header} ${isPublic ? styles.public : ''}`} id="header" style={headerElementStyle}>
      <EuHeader euHeaderElementStyle={euHeaderElementStyle} globanElementStyle={globanElementStyle} />
      <div className={`${styles.customHeader} ${isPublic ? styles.public : ''}`}>
        {loadTitle()}
        {<BreadCrumb isPublic={isPublic} />}
        {!isPublic && loadUser()}
        {isPublic && loadLogin()}
        {!isPublic && userContext.userProps.showLogoutConfirmation && confirmvisible && (
          <ConfirmDialog
            footerAddon={checkDoNotRemember}
            header={resources.messages['logout']}
            labelCancel={resources.messages['no']}
            labelConfirm={resources.messages['yes']}
            onConfirm={() => {
              userLogout();
            }}
            onHide={() => setConfirmVisible(false)}
            visible={confirmvisible}>
            {resources.messages['userLogout']}
          </ConfirmDialog>
        )}
      </div>
    </div>
  );
});
export { Header };
