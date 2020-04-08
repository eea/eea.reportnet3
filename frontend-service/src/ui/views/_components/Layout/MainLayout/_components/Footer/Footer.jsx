import React, { useContext, Fragment } from 'react';

import styles from './Footer.module.css';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const Footer = () => {
  const resources = useContext(ResourcesContext);
  const footerLinks = [
    { text: 'copyrightAbout' },
    { text: 'copyrightLanguage' },
    { text: 'copyrightResources' },
    { text: 'copyrightCookies' },

    { text: 'copyrightLegal' },
    { text: 'copyrightContact' }
  ];
  return (
    <Fragment>
      <footer className={`${styles.FooterExpanded} ${styles.Footer}`}>
        {footerLinks.map((footerLink, i) => (
          <Fragment key={i}>
            <a type="button" disabled style={{ cursor: 'pointer' }}>
              {resources.messages[footerLink.text]}
            </a>
            {i < footerLinks ? '·' : ''}
          </Fragment>
        ))}
      </footer>
    </Fragment>
  );
};
