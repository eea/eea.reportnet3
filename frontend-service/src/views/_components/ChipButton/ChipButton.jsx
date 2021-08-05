import { useEffect, useRef, useState } from 'react';

import isNil from 'lodash/isNil';

import styles from './ChipButton.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Icon } from 'views/_components/Icon';
import Tooltip from 'primereact/tooltip';

export const ChipButton = ({
  className = '',
  hasLevelErrorIcon = false,
  key,
  icon = null,
  labelClassName,
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
        <Icon
          className={`${styles.chipButtonIcon} ${styles[levelError.toLowerCase()]}`}
          icon={levelError.toUpperCase() === 'BLOCKER' ? 'blocker' : 'warning'}
        />
      )}
      {icon && <FontAwesomeIcon aria-hidden={false} icon={AwesomeIcons(icon)} />}
      <span className={`${labelClassName} ${styles.chipButtonLabel}`} ref={inputElement}>
        {value}
      </span>
      <Icon
        className={styles.chipButtonIcon}
        icon={iconToShow}
        onClick={onClick}
        onMouseOut={() => setIconToShow('cancel')}
        onMouseOver={() => setIconToShow('errorCircle')}
      />
    </div>
  );
};
