import React, { useContext } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import styles from './Card.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const Card = ({ checked, date, icon, id, obligation, onCheck, subtitle, title }) => {
  const resources = useContext(ResourcesContext);

  return (
    <div
      className={`${styles.card} ${checked.id === id ? styles.checked : undefined}`}
      onClick={() => onCheck(obligation)}>
      <div className={styles.text}>
        <h3 className={styles.title}>{title}</h3>
        <p className={styles.subtitle}>{subtitle}</p>
      </div>

      <div className={`${styles.link}`}>
        <FontAwesomeIcon
          aria-hidden={false}
          className={styles.linkIcon}
          icon={AwesomeIcons(icon)}
          onMouseDown={() => window.open(`http://rod3.devel1dub.eionet.europa.eu/obligations/${id}`)}
        />
      </div>

      <div className={`${styles.date}`}>
        {resources.messages['nextReportDue']}: <span className={styles.dueDate}>{date}</span>
      </div>
    </div>
  );
};
