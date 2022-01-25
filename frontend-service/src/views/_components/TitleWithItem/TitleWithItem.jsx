import { memo } from 'react';
import ReactDOMServer from 'react-dom/server';

import uniqueId from 'lodash/uniqueId';

import pushNotificationInfo from 'views/_assets/images/gifs/pushNotificationInfo.gif';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { TooltipButton } from 'views/_components/TooltipButton';

import styles from './TitleWithItem.module.scss';

export const TitleWithItem = memo(
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
                <TooltipButton
                  getContent={() =>
                    ReactDOMServer.renderToStaticMarkup(
                      <div className={styles.infoTooltipWrapper}>
                        <span>{tooltipInfo}</span>
                        <img alt={tooltipInfo} className={imgClassName} src={pushNotificationInfo}></img>
                      </div>
                    )
                  }
                  tooltipClassName={styles.infoTooltip}
                  uniqueIdentifier={tooltipId}></TooltipButton>
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
