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
  externalCard,
  frequency,
  isReleasable,
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
    e.preventDefault();
    window.open(url, '_blank');
    e.stopPropagation();
  };

  const renderRedirectText = (text, url) => (
    <Fragment>
      <span>{text} </span>
      <a href={url} onClick={e => onOpenTab(e, url)} rel="noreferrer" target="_blank" title={text}>
        <FontAwesomeIcon
          aria-hidden={false}
          aria-label={text}
          className="p-breadcrumb-home"
          icon={AwesomeIcons('externalUrl')}
        />
      </a>
    </Fragment>
  );

  const externalCardLayout = children => {
    return (
      <div className={styles.card} onClick={e => onOpenTab(e, card.dataFlowUrl)}>
        {children}
      </div>
    );
  };

  const internalCardLayout = children => {
    return (
      <div className={`${styles.card} ${animation ? styles.clickable : ''}`} onClick={() => onCardClick(dataflowId)}>
        {children}
      </div>
    );
  };

  const getCardBody = () => {
    return (
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

        {externalCard ? (
          <div className={styles.legalInstrumentAndObligation}>
            <p>
              <strong>{resourcesContext.messages['pilotScenarioAmbitionColumnTitle']}: </strong> {pilotScenarioAmbition}
            </p>
          </div>
        ) : (
          <Fragment>
            <div className={styles.legalInstrumentAndObligation}>
              <p>
                <strong>{resourcesContext.messages['obligationTitle']}: </strong>
                {obligation?.obligationId
                  ? renderRedirectText(obligation?.title, `${baseRod3Url}/obligations/${obligation?.obligationId}`)
                  : obligation?.title}
              </p>
            </div>
            <div className={styles.legalInstrumentAndObligation}>
              <p>
                <strong>{resourcesContext.messages['instrumentColumnTitle']}: </strong>
                {obligation?.legalInstruments?.id
                  ? renderRedirectText(
                      obligation?.legalInstruments?.alias,
                      `${baseRod3Url}/instruments/${obligation?.legalInstruments?.id}`
                    )
                  : obligation?.legalInstruments?.alias}
              </p>
            </div>
          </Fragment>
        )}
        <div className={`${styles.footer}`}>
          {externalCard ? (
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
  };

  if (externalCard) {
    return externalCardLayout(getCardBody());
  }
  return internalCardLayout(getCardBody());
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
