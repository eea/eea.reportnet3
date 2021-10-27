import { Fragment, useContext } from 'react';
import PropTypes from 'prop-types';

import uniqueId from 'lodash/uniqueId';

import styles from './PublicCard.module.scss';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import ReactTooltip from 'react-tooltip';

export const PublicCard = ({
  animation,
  card,
  dataflowId,
  dueDate,
  frequency,
  landingPageCard,
  obligation,
  onCardClick,
  pilotScenarioAmbition,
  status,
  subtitle,
  title
}) => {
  const resourcesContext = useContext(ResourcesContext);

  const idTooltip = uniqueId();
  const baseRod3Url = 'https://rod.eionet.europa.eu';

  const onOpenTab = (e, url) => {
    if (e.button === 0 || e.button === 1) {
      e.preventDefault();
      window.open(url, '_blank');
      e.stopPropagation();
    }
  };

  const renderRedirectText = (text, url) => (
    <span>
      {text}{' '}
      <span className={styles.link} onMouseDown={e => onOpenTab(e, url)} title={text}>
        <FontAwesomeIcon
          aria-hidden={false}
          aria-label={url}
          className="p-breadcrumb-home"
          icon={AwesomeIcons('externalUrl')}
          role="button"
        />
      </span>
    </span>
  );

  const renderLandingPageDataflowLayout = children => {
    return (
      <div className={styles.card} onClick={e => onOpenTab(e, card.dataFlowUrl)}>
        {children}
      </div>
    );
  };

  const onOpenDataflow = (e, dataflowId) => {
    if (e.button === 0) {
      onCardClick(dataflowId, false);
    } else if (e.button === 1) {
      onCardClick(dataflowId, true);
    }
  };

  const renderPublicDataflowLayout = children => (
    <div
      className={`${styles.card} ${animation ? styles.clickable : ''}`}
      onMouseDown={e => onOpenDataflow(e, dataflowId)}>
      {children}
    </div>
  );

  const getCardBody = () => (
    <div className={styles.content}>
      <div className={styles.text}>
        <h3 className={`${styles.title} ${styles.link}`}>{title.text}</h3>
        <h4 className={styles.subtitle} data-for={idTooltip} data-tip>
          {subtitle.url ? renderRedirectText(subtitle.text, subtitle.url) : subtitle.text}
        </h4>
        <ReactTooltip border={true} className={styles.tooltip} effect="solid" id={idTooltip} place="top">
          {subtitle.text}
        </ReactTooltip>
      </div>

      {landingPageCard ? (
        <div className={styles.legalInstrumentAndObligation}>
          <p>
            <strong>{resourcesContext.messages['pilotScenarioAmbitionColumnTitle']}: </strong> {pilotScenarioAmbition}
          </p>
        </div>
      ) : (
        <Fragment>
          <div className={styles.legalInstrumentAndObligation}>
            <p>
              <strong>{resourcesContext.messages['obligation']}: </strong>
              {obligation?.obligationId
                ? renderRedirectText(obligation?.title, `${baseRod3Url}/obligations/${obligation?.obligationId}`)
                : obligation?.title}
            </p>
          </div>
          <div className={styles.legalInstrumentAndObligation}>
            <p>
              <strong>{resourcesContext.messages['instrumentColumnTitle']}: </strong>
              {obligation?.legalInstrument?.id
                ? renderRedirectText(
                    obligation?.legalInstrument?.alias,
                    `${baseRod3Url}/instruments/${obligation?.legalInstrument?.id}`
                  )
                : obligation?.legalInstrument?.alias}
            </p>
          </div>
        </Fragment>
      )}
      <div className={`${styles.footer}`}>
        {landingPageCard ? (
          <span>
            <strong>{resourcesContext.messages['frequencyColumnTitle']}: </strong>
            {frequency}
          </span>
        ) : (
          <span>
            <strong>{resourcesContext.messages['status']}: </strong>
            {status}
          </span>
        )}
        <span>
          <strong>{resourcesContext.messages['deliveryDate']}:</strong> {dueDate}
        </span>
      </div>
    </div>
  );

  return landingPageCard ? renderLandingPageDataflowLayout(getCardBody()) : renderPublicDataflowLayout(getCardBody());
};

PublicCard.propTypes = {
  animation: PropTypes.bool,
  onCardClick: PropTypes.func,
  subtitle: PropTypes.object,
  title: PropTypes.object
};

PublicCard.defaultProps = {
  animation: false,
  onCardClick: () => {},
  subtitle: { text: '', url: '' },
  title: { text: '', url: '' }
};
