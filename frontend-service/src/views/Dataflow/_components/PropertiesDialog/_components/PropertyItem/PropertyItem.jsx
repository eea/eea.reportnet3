import { useContext, useState } from 'react';
import PropTypes from 'prop-types';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import styles from './PropertyItem.module.scss';

import { Button } from 'views/_components/Button';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import isEmpty from 'lodash/isEmpty';

export const PropertyItem = ({ content, title, redirectTo }) => {
  const resourcesContext = useContext(ResourcesContext);

  const [isOpen, setIsOpen] = useState(true);

  const onToggleVisibility = () => setIsOpen(prevState => !prevState);

  return (
    <div style={{ marginTop: '1rem', marginBottom: '2rem' }}>
      <h3 className={styles.title}>
        <FontAwesomeIcon
          className={styles.icon}
          icon={AwesomeIcons(isOpen ? 'angleDown' : 'angleRight')}
          onClick={onToggleVisibility}
        />
        {title}

        {!isEmpty(redirectTo) && (
          <Button
            className={'p-button-secondary-transparent'}
            icon={'externalUrl'}
            onMouseDown={() => window.open(redirectTo)}
            tooltip={resourcesContext.messages['viewMore']}
          />
        )}
      </h3>
      <div className={`${styles.content} ${isOpen ? '' : styles.hide}`}>
        {content.map(item => (
          <span key={item.id}>
            <strong>{resourcesContext.messages[item.labelKey]}</strong>
            {item.labelValue || '-'}
          </span>
        ))}
      </div>
    </div>
  );
};

PropertyItem.propTypes = {
  content: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.number.isRequired,
      labelKey: PropTypes.string.isRequired,
      labelValue: PropTypes.string.isRequired
    })
  ).isRequired,
  redirectTo: PropTypes.string,
  title: PropTypes.string
};

PropertyItem.defaultProps = {
  content: [],
  redirectTo: '',
  title: ''
};
