import React from 'react';
import { withRouter } from 'react-router-dom';

import styles from './UserLinks.module.scss';

import { routes } from 'ui/routes';
import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { getUrl } from 'core/infrastructure/CoreUtils';

const UserLinks = withRouter(({ history }) => {
  return (
    <div className={styles.linksContainer}>
      {/* TO DO CHOOSE ICONS AND URLS */}
      <div></div>

      <div></div>

      <a
        href={getUrl(routes.DATAFLOWS)}
        title="First Icon"
        onClick={async e => {
          e.preventDefault();
          history.push(getUrl(routes.DATAFLOWS));
        }}>
        <FontAwesomeIcon className={styles.link} icon={AwesomeIcons('user-profile')} />
      </a>

      <a
        href={getUrl(routes.DATAFLOWS)}
        title="Second Icon"
        onClick={async e => {
          e.preventDefault();
          history.push(getUrl(routes.DATAFLOWS));
        }}>
        <FontAwesomeIcon className={styles.link} icon={AwesomeIcons('user-profile')} />
      </a>

      <a
        href={getUrl(routes.DATAFLOWS)}
        title="Third Icon"
        onClick={async e => {
          e.preventDefault();
          history.push(getUrl(routes.DATAFLOWS));
        }}>
        <FontAwesomeIcon className={styles.link} icon={AwesomeIcons('user-profile')} />
      </a>

      <div></div>

      <div></div>
    </div>
  );
});

export { UserLinks };
