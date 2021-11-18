import { useContext } from 'react';
import PropTypes from 'prop-types';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import styles from './Card.module.scss';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const Card = ({
  card,
  checked,
  date,
  handleRedirect,
  icon,
  id,
  isReferenceDataflow,
  onCheck,
  status,
  subtitle,
  title,
  type
}) => {
  const resourcesContext = useContext(ResourcesContext);

  const isCloneSchemasView = type === 'cloneSchemas';
  const isSelected = checked.id === id ? styles.checked : undefined;

  const renderCardFooter = () =>
    isCloneSchemasView && (
      <span>
        {resourcesContext.messages['status']}: <span className={styles.dueDate}>{status}</span>
      </span>
    );

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
        {!isReferenceDataflow && (
          <span>
            {isCloneSchemasView ? resourcesContext.messages['date'] : resourcesContext.messages['nextReportDue']}:
            <span className={styles.dueDate}>{date}</span>
          </span>
        )}
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
