import React, { useContext } from 'react';
import { withRouter } from 'react-router-dom';

import logo from 'assets/images/logo.png';
import styles from './Header.module.scss';

import { routes } from 'ui/routes';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { InputSwitch } from 'ui/views/_components/InputSwitch';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';

import { getUrl } from 'core/infrastructure/CoreUtils';

const Header = withRouter(({ history }) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);
  const themeContext = useContext(ThemeContext);

  const loadTitle = () => (
    <a
      href={getUrl(routes.DATAFLOWS)}
      className={styles.title}
      title={resources.messages['titleHeader']}
      onClick={e => {
        e.preventDefault();
        history.push(getUrl(routes.DATAFLOWS));
      }}>
      <img height="50px" src={logo} alt="Reportnet" className={styles.appLogo} />
      <h1 className={styles.appTitle}>{resources.messages['titleHeader']}</h1>
    </a>
  );
  const loadUser = () => (
    <div className={styles.userWrapper}>
      {console.log(themeContext)}
      <InputSwitch
        checked={themeContext.currentTheme === 'dark'}
        onChange={e => themeContext.onToggleTheme(e.value ? 'dark' : 'light')}
        sliderCheckedClassName={styles.themeSwitcherInputSwitch}
        style={{ marginRight: '1rem' }}
        tooltip={
          themeContext.currentTheme === 'light'
            ? resources.messages['toggleDarkTheme']
            : resources.messages['toggleLightTheme']
        }
        tooltipOptions={{ position: 'bottom', className: styles.themeSwitcherTooltip }}
      />
      <span>{`@${userContext.preferredUsername}`}</span>
    </div>
  );
  return (
    <div id="header" className={styles.header}>
      {loadTitle()}
      <BreadCrumb />
      {loadUser()}
    </div>
  );
});
export { Header };
