import styles from './TooltipButton.module.scss';
import ReactTooltip from 'react-tooltip';
import { Button } from 'ui/views/_components/Button';

export const TooltipButton = ({ message, uniqueIdentifier, getContent = null }) => {
  return (
    <>
      <span data-for={`infoCircleButton_${uniqueIdentifier}`} data-tip>
        <Button
          className={`${styles.tooltipButton} p-button-rounded p-button-secondary-transparent`}
          icon="infoCircle"
        />
      </span>
      <ReactTooltip
        border={true}
        effect="solid"
        getContent={() => (getContent ? getContent() : message)}
        html={true}
        id={`infoCircleButton_${uniqueIdentifier}`}
        place="top"
      />
    </>
  );
};
