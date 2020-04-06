import React, { useContext } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import styles from './Card.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { Checkbox } from 'ui/views/_components/Checkbox';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const Card = ({ checked, date, icon, id, obligation, onCheck, subtitle, title }) => {
  const resources = useContext(ResourcesContext);

  return (
    <div className={styles.card}>
      <div className={styles.text}>
        <h3 className={styles.title}>{title}</h3>
        <p className={styles.subtitle}>{subtitle}</p>
      </div>

      <div className={`${styles.link}`}>
        <FontAwesomeIcon
          className={styles.linkIcon}
          icon={AwesomeIcons(icon)}
          onMouseDown={() => window.open(`http://rod3.devel1dub.eionet.europa.eu/obligations/${id}`)}
        />
      </div>

      <div className={`${styles.date}`}>
        {resources.messages['dueDate']}: <span className={styles.dueDate}>{date}</span>
      </div>

      <div className={`${styles.toolbar}`}>
        <Checkbox
          id={`${id}_checkbox`}
          isChecked={checked.id === id}
          onChange={() => onCheck(obligation)}
          role="checkbox"
        />
      </div>
    </div>
  );
};
