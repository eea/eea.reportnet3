import { Fragment } from 'react';

import styles from './TooltipButton.module.scss';
import ReactTooltip from 'react-tooltip';
import { Button } from 'views/_components/Button';

export const TooltipButton = ({
  getContent = null,
  message,
  onClick = () => {},
  tabIndex = '-1',
  tooltipClassName = '',
  uniqueIdentifier = 1,
  maxWidth = false
}) => {
  return (
    <Fragment>
      <span data-for={`infoCircleButton_${uniqueIdentifier}`} data-tip>
        <Button
          className={`${styles.tooltipButton} p-button-rounded p-button-secondary-transparent`}
          icon="infoCircle"
          onClick={onClick}
          role="button"
          tabIndex={tabIndex}
        />
      </span>
      <ReactTooltip
        border={true}
        className={`${tooltipClassName} ${maxWidth ? styles.maxWidth : null}`}
        effect="solid"
        getContent={() => (getContent ? getContent() : message)}
        html={true}
        id={`infoCircleButton_${uniqueIdentifier}`}
        place="top"
      />
    </Fragment>
  );
};
