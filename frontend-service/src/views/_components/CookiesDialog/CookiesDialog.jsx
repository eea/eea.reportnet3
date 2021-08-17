import { useContext, useEffect, useState } from 'react';

import startsWith from 'lodash/startsWith';

import styles from './CookiesDialog.module.scss';

import { ThemeContext } from 'views/_functions/Contexts/ThemeContext';

export const CookiesDialog = () => {
  const themeContext = useContext(ThemeContext);
  const [isVisible, setIsVisible] = useState(false);

  const isEuCookie = () => {
    const decodedCookies = decodeURIComponent(document.cookie);
    const cookiesArray = decodedCookies.split('; ');
    for (let i = 0; i <= cookiesArray.length; i++) {
      const cookie = cookiesArray[i];
      if (startsWith(cookie, 'eu_cookie_consent=')) {
        return true;
      }
    }
    return false;
  };

  useEffect(() => {
    if (isEuCookie()) {
      setIsVisible(false);
      themeContext.setHeaderCollapse(true);
    } else {
      setIsVisible(true);
    }
  }, []);

  const onAcceptCookies = () => {
    document.cookie = 'eu_cookie_consent=true;path=/';
    setIsVisible(false);
    themeContext.setHeaderCollapse(true);
  };
  const onRefuseCookies = () => {
    document.cookie = 'eu_cookie_consent=false;path=/';
    setIsVisible(false);
    themeContext.setHeaderCollapse(true);
  };

  return (
    isVisible && (
      <div className={styles.Wrapper}>
        <h3>Cookies</h3>
        <p>
          This site uses cookies to offer you a better browsing experience. Find out more on how we use cookies and{' '}
          <a href="https://ec.europa.eu/info/cookies_en" rel="noopener noreferrer" target="_blank">
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
