import React, { Fragment, useContext } from 'react';
import PropTypes from 'prop-types';

import isNil from 'lodash/isNil';

import styles from './PublicCard.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const PublicCard = ({ dataflowId, dueDate, frequency, onCardClick, pilotScenarioAmbition, subtitle, title }) => {
  const resources = useContext(ResourcesContext);

  const renderRedirectText = (text, url) => (
    <a href={url} target="_blank" title={text}>
      <span>
        {text} <FontAwesomeIcon aria-hidden={false} className="p-breadcrumb-home" icon={AwesomeIcons('externalLink')} />
      </span>
    </a>
  );

  return (
    <div
      className={`${styles.card} ${!isNil(onCardClick) ? styles.clickable : undefined}`}
      onClick={() => onCardClick(dataflowId)}>
      <div className={styles.content}>
        <div className={styles.text}>
          <h3 className={styles.title} title={title.text}>
            {renderRedirectText(title.text, title.url)}
          </h3>
          <h4 className={styles.subtitle} title={subtitle.text}>
            {subtitle.url ? renderRedirectText(subtitle.text, subtitle.url) : subtitle.text}
          </h4>
        </div>
        <div className={styles.pilotScenarioAmbition}>
          <p>
            <strong>Pilot scenario ambition: </strong> {pilotScenarioAmbition}
          </p>
        </div>

        <div className={`${styles.footer}`}>
          <span>
            {frequency && (
              <Fragment>
                <strong>Frequency:</strong> {frequency}
              </Fragment>
            )}
          </span>
          <span>
            <strong>Delivery date:</strong> {dueDate}
          </span>
        </div>
      </div>
    </div>
  );
};

PublicCard.propTypes = { onCardClick: PropTypes.func, subtitle: PropTypes.object, title: PropTypes.object };

PublicCard.defaultProps = { onCardClick: null, subtitle: { text: '', url: '' }, title: { text: '', url: '' } };
