import React, { useContext, useState } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import styles from './PropertyItem.module.scss';

export const PropertyItem = ({ onRedirect, title }) => {
  const resourcesContext = useContext(ResourcesContext);

  const [isOpen, setIsOpen] = useState(true);

  return (
    <div>
      <div className={styles.titleWrapper}>
        <h3 className={styles.title}>
          <FontAwesomeIcon
            className={styles.icon}
            icon={AwesomeIcons(isOpen ? 'angleDown' : 'angleRight')}
            onClick={() => setIsOpen(!isOpen)}
          />
          {title}
        </h3>
      </div>
    </div>
  );
};
