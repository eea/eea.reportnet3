import React, { useContext, useState } from 'react';

import styles from './Footer.module.css';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const Footer = () => {
  const [isExpanded, setIsExpanded] = useState(false);
  const resources = useContext(ResourcesContext);
  return (
    <React.Fragment>
      <div className={styles.slideUpFooter} onClick={() => setIsExpanded(!isExpanded)}>
        <span className={styles.slideUpFooterSpan}>
          <FontAwesomeIcon icon={AwesomeIcons('angleUp')} className={styles.slideUpFooterButton} />
        </span>
      </div>
      <footer className={`${isExpanded ? styles.FooterExpanded : styles.FooterCollapsed} ${styles.Footer}`}>
        <a href=".">{resources.messages['copyrightAbout']}</a>
        <a href=".">{resources.messages['copyrightLanguage']}</a>
        <a href=".">{resources.messages['copyrightResources']}</a>
        <a href=".">{resources.messages['copyrightCookies']}</a>
        <a href=".">{resources.messages['copyrightPrivacy']}</a>
        <a href=".">{resources.messages['copyrightLegal']}</a>
        <a href=".">{resources.messages['copyrightContact']}</a>
      </footer>
    </React.Fragment>
  );
};
