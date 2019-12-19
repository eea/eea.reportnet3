import React, { useContext } from 'react';

import styles from './Footer.module.css';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const Footer = () => {
  const resources = useContext(ResourcesContext);
  return (
    <footer className={styles.Footer}>
      <a href=".">{resources.messages['copyrightAbout']}</a>
      <a href=".">{resources.messages['copyrightLanguage']}</a>
      <a href=".">{resources.messages['copyrightResources']}</a>
      <a href=".">{resources.messages['copyrightCookies']}</a>
      <a href=".">{resources.messages['copyrightPrivacy']}</a>
      <a href=".">{resources.messages['copyrightLegal']}</a>
      <a href=".">{resources.messages['copyrightContact']}</a>
    </footer>
  );
};
