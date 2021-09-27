import { useContext, useState } from 'react';
import PropTypes from 'prop-types';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import styles from './PropertyItem.module.scss';

import { Button } from 'views/_components/Button';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { RodUrl } from 'repositories/config/RodUrl';

export const PropertyItem = ({ content, title }) => {
  const resourcesContext = useContext(ResourcesContext);

  const [isOpen, setIsOpen] = useState(true);

  const onToggleVisibility = () => setIsOpen(prevState => !prevState);

  const renderContent = () =>
    content.map(item => (
      <span key={item.id}>
        <strong>{item.labelKey}</strong>
        {item.labelValue || '-'}
      </span>
    ));

  return (
    <div style={{ marginTop: '1rem', marginBottom: '2rem' }}>
      <h3 className={styles.title}>
        <FontAwesomeIcon
          className={styles.icon}
          icon={AwesomeIcons(isOpen ? 'angleDown' : 'angleRight')}
          onClick={onToggleVisibility}
        />
        {title}

        {/* <Button
          className={'p-button-secondary-transparent'}
          icon={'externalUrl'}
          onMouseDown={() => window.open(`${RodUrl.content}${renderContent.obligationId}`)}
          tooltip={resourcesContext.messages['viewMore']}
        /> */}
      </h3>
      <div className={`${styles.content} ${isOpen ? '' : styles.hide}`}>{renderContent()}</div>
    </div>
  );
};

PropertyItem.propTypes = {
  title: PropTypes.string,
  content: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.number.isRequired,
      labelKey: PropTypes.string.isRequired,
      labelValue: PropTypes.string.isRequired
    })
  ).isRequired
};

PropertyItem.defaultProps = {
  content: [],
  title: ''
};
