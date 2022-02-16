import { memo } from 'react';
import styles from './Step.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

export const Step = memo(({ currentStep, step }) => {
  const getIconClassName = () => {
    if (step?.stepNumber === currentStep && step?.isRunning) {
      return 'fa-spin';
    }
  };

  const getIconWrapperClassName = () => {
    if (step.completed && !step.withError) {
      return styles.activeCompleted;
    } else if (step.completed && step.withError) {
      return styles.withError;
    } else {
      if (step.stepNumber < currentStep) {
        return styles.activeCompleted;
      } else if (step.stepNumber === currentStep) {
        if (step.isRunning) {
          return styles.activeIncompleted;
        } else {
          return styles.activeCompleted;
        }
      } else {
        return styles.inactive;
      }
    }
  };

  const getStepClassName = () => {
    if (step.stepNumber <= currentStep) {
      return styles.stepActive;
    }
  };

  const getStepLabel = () => {
    if (step.completed && !step.withError) {
      return step.labelCompleted;
    } else if (step.completed && step.withError) {
      return step.labelError;
    } else {
      if (step.stepNumber < currentStep) {
        return step.labelCompleted;
      } else {
        if (step.stepNumber === currentStep) {
          if (step.isRunning) {
            return step.labelRunning;
          } else {
            return step.labelCompleted;
          }
        } else {
          return step.labelUndone;
        }
      }
    }
  };

  const renderIcon = () => {
    if (step.completed && step.withError) {
      return AwesomeIcons('cross');
    } else if (step.stepNumber < currentStep) {
      return AwesomeIcons('check');
    } else if (step.stepNumber === currentStep) {
      if (step.isRunning) {
        return AwesomeIcons('spinner');
      } else {
        return AwesomeIcons('check');
      }
    } else {
      return AwesomeIcons('clock');
    }
  };

  return (
    <li className={`${styles.step} ${getStepClassName()}`}>
      <div className={`${styles.iconWrapper} ${getIconWrapperClassName()}`}>
        <FontAwesomeIcon className={getIconClassName()} icon={renderIcon()} />
      </div>
      <label className={styles.stepLabel}>{getStepLabel()}</label>
    </li>
  );
});
