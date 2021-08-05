import { Fragment, memo } from 'react';

import uniqueId from 'lodash/uniqueId';

import pushNotificationInfo from 'views/_assets/images/gifs/pushNotificationInfo.gif';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import ReactTooltip from 'react-tooltip';

import styles from './TitleWithItem.module.scss';

const TitleWithItem = memo(
  ({
    hasInfoTooltip = false,
    icon,
    iconSize,
    imgClassName = '',
    items,
    subtitle,
    title,
    tooltipInfo = 'Info tooltip'
  }) => {
    const tooltipId = uniqueId();

    return (
      <div className={styles.rowContainer}>
        <div className={styles.titleWrap}>
          <div className={styles.iconWrap}>
            <FontAwesomeIcon
              className={styles.icon}
              icon={AwesomeIcons(icon)}
              role="presentation"
              style={{ fontSize: iconSize }}
            />
          </div>
          <div className={styles.textWrap}>
            <span className={styles.title}>
              {title}
              {hasInfoTooltip && (
                <Fragment>
                  <FontAwesomeIcon
                    aria-hidden={false}
                    aria-label="Info"
                    className={`${styles.infoButton} p-button-rounded p-button-secondary-transparent`}
                    data-for={tooltipId}
                    data-tip
                    icon={AwesomeIcons('infoCircle')}
                  />

                  <ReactTooltip border={true} className={styles.tooltip} effect="solid" id={tooltipId} place="right">
                    {
                      <div className={styles.infoTooltipWrapper}>
                        <span>{tooltipInfo}</span>
                        <img alt={tooltipInfo} className={imgClassName} src={pushNotificationInfo}></img>
                      </div>
                    }
                  </ReactTooltip>
                </Fragment>
              )}
            </span>
            <span className={styles.subtitle}>{subtitle}</span>
          </div>
        </div>
        <div className={styles.itemsContainer}>
          {items.map((item, i) => (
            <div className={styles.itemContainer} key={uniqueId()}>
              {item}
            </div>
          ))}
        </div>
      </div>
    );
  }
);

export { TitleWithItem };
