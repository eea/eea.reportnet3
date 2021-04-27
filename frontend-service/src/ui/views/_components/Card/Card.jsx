import { Fragment, useContext } from 'react';
import PropTypes from 'prop-types';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import styles from './Card.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const Card = ({ card, checked, date, handleRedirect, icon, id, onCheck, status, subtitle, title, type }) => {
  const resources = useContext(ResourcesContext);

  const isCloneSchemasView = type === 'cloneSchemas';
  const isSelected = checked.id === id ? styles.checked : undefined;

  const renderCardFooter = () => {
    if (isCloneSchemasView) {
      return (
        <span>
          {resources.messages['status']}: <span className={styles.dueDate}>{status}</span>
        </span>
      );
    } else return <Fragment />;
  };

  return (
    <div className={`${styles.card} ${isSelected} ${styles[status]}`} onClick={() => onCheck(card)}>
      <div className={styles.text}>
        <h3 className={styles.title}>{title}</h3>
        <p className={styles.subtitle}>{subtitle}</p>
      </div>

      <div className={`${styles.link}`}>
        <FontAwesomeIcon
          aria-hidden={false}
          className={styles.linkIcon}
          icon={AwesomeIcons(icon)}
          onMouseDown={() => handleRedirect(id)}
        />
      </div>

      <div className={`${styles.date} ${styles[type]}`}>
        <span>
          {resources.messages[isCloneSchemasView ? 'date' : 'nextReportDue']}:
          <span className={styles.dueDate}>{date}</span>
        </span>
        {renderCardFooter()}
      </div>
    </div>
  );
};

Card.propTypes = {
  checked: PropTypes.object,
  handleRedirect: PropTypes.func,
  onCheck: PropTypes.func
};

Card.defaultProps = {
  checked: { id: null, title: '' },
  handleRedirect: () => {},
  onCheck: () => {}
};
