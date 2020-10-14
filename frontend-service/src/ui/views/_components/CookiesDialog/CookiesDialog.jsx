import React, { useContext, useEffect, useState } from 'react';

import isUndefined from 'lodash/isUndefined';

import styles from './CookiesDialog.module.scss';

import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';

import { userStorage } from 'core/domain/model/User/UserStorage';

export const CookiesDialog = () => {
  const themeContext = useContext(ThemeContext);
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    const cookieConsent = userStorage.getPropertyFromLocalStorage('cookieConsent');
    if (isUndefined(cookieConsent)) {
      setIsVisible(true);
    } else {
      setIsVisible(false);
    }
  }, []);

  useEffect(() => {
    themeContext.setHeaderCollapse(!isVisible);
  }, [isVisible]);

  const onAcceptCookies = () => {
    userStorage.setPropertyToLocalStorage({ cookieConsent: true });
    setIsVisible(false);
  };
  const onRefuseCookies = () => {
    userStorage.setPropertyToLocalStorage({ cookieConsent: false });
    setIsVisible(false);
  };

  return (
    isVisible && (
      <div className={styles.Wrapper}>
        <h3>Cookies</h3>
        <p>
          This site uses cookies to offer you a better browsing experience. Find out more on how we use cookies and{' '}
          <a target="_blank" href="https://ec.europa.eu/info/cookies_en">
            how you can change your settings
          </a>
          .
        </p>
        <div>
          <button onClick={() => onAcceptCookies()}>I accept cookies</button>
          <button onClick={() => onRefuseCookies()}>I refuse cookies</button>
        </div>
      </div>
    )
  );
};
