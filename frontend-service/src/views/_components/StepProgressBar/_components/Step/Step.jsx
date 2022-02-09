import { useEffect, useState } from 'react';
import styles from './Step.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

export const Step = ({ currentStep, step }) => {
  const [stepClass, setStepClass] = useState('');

  useEffect(() => {
    if (step.completed) {
      if (stepClass !== styles.completed) {
        setStepClass(styles.activeCompleted);
      }
    } else {
      if (step.idx < currentStep) {
        if (stepClass !== styles.activeCompleted) {
          setStepClass(styles.activeCompleted);
        }
      } else if (step.idx === currentStep) {
        if (step.isRunning) {
          if (stepClass !== styles.activeCompleted) {
            setStepClass(styles.activeIncompleted);
          }
        } else {
          if (stepClass !== styles.activeCompleted) {
            setStepClass(styles.activeCompleted);
          }
        }
      } else {
        if (stepClass !== styles.inactive) {
          setStepClass(styles.inactive);
        }
      }
    }
  }, [step]);

  const getIconClassName = () => {
    if (step?.idx === currentStep && step?.isRunning) {
      return 'fa-spin';
    }
  };

  const getIconWrapperClassName = () => {
    return stepClass;
  };

  const getStepClassName = () => {
    if (step.idx <= currentStep) {
      return styles.stepActive;
    }
  };

  const getStepLabel = () => {
    if (step.completed) {
      return step.labelCompleted;
    } else {
      if (step.idx < currentStep) {
        return step.labelCompleted;
      } else {
        if (step.idx === currentStep) {
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
    if (step.idx < currentStep) {
      return AwesomeIcons('check');
    } else if (step.idx === currentStep) {
      if (step.isRunning) {
        return AwesomeIcons('spinner');
      } else {
        return AwesomeIcons('check');
      }
    } else {
      return AwesomeIcons('cross');
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
};
