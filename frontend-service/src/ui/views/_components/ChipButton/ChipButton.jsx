import { useEffect, useRef, useState } from 'react';

import isNil from 'lodash/isNil';

import styles from './ChipButton.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Icon } from 'ui/views/_components/Icon';
import { IconTooltip } from 'ui/views/_components/IconTooltip';
import Tooltip from 'primereact/tooltip';

export const ChipButton = ({
  className = '',
  hasLevelErrorIcon = false,
  key,
  icon = null,
  levelError,
  onClick,
  style,
  tooltip,
  tooltipOptions,
  value
}) => {
  const inputElement = useRef();
  const listElement = useRef();
  const [iconToShow, setIconToShow] = useState('cancel');

  useEffect(() => {
    if (!isNil(tooltip)) {
      renderTooltip();
    }
  }, []);

  const renderTooltip = () => {
    new Tooltip({
      target: listElement.current,
      targetContainer: listElement.current,
      content: tooltip,
      options: tooltipOptions
    });
  };

  return (
    <div className={`${className} ${styles.chipButton}`} key={key} ref={listElement} style={style}>
      {hasLevelErrorIcon && (
        <IconTooltip
          className={styles.chipButtonErrorIcon}
          key={'levelErrorIcon'}
          levelError={levelError.toUpperCase()}
          message={''}
        />
      )}
      {icon && <FontAwesomeIcon aria-hidden={false} icon={AwesomeIcons(icon)} />}
      <span className={`${styles.labelClassName} ${styles.chipButtonLabel}`} ref={inputElement}>
        {value}
      </span>
      <div onMouseOut={() => setIconToShow('cancel')} onMouseOver={() => setIconToShow('errorCircle')}>
        <Icon className={styles.chipButtonIcon} icon={iconToShow} onClick={onClick} />
      </div>
    </div>
  );
};
