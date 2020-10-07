import React, { useState, useEffect } from 'react';

import styles from './CookiesDialog.module.scss';

import { userStorage } from 'core/domain/model/User/UserStorage';

export const CookiesDialog = () => {
  const [isVisible, setIsVisible] = useState(true);

  useEffect(() => {
    const cookieConsent = userStorage.getPropertyFromLocalStorage('cookieConsent');
    if (cookieConsent) {
      setIsVisible(cookieConsent);
    } else {
      setIsVisible(true);
    }
  }, []);

  useEffect(() => {
    if (isVisible) {
      const header = window.document.querySelector('.header');
    }
  }, [isVisible]);

  return (
    isVisible && (
      <div className={styles.Wrapper}>
        <h3>Cookies</h3>
        <p>
          This site uses cookies to offer you a better browsing experience. Find out more on how we use cookies and{' '}
          <a href="https://ec.europa.eu/info/cookies_en">how you can change your settings</a>.
        </p>
        <div>
          <button>I accept cookies</button>
          <button>I refuse cookies</button>
        </div>
      </div>
    )
  );
};
