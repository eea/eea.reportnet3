import isEmpty from 'lodash/isEmpty';
import uniqueId from 'lodash/uniqueId';

import styles from './StepProgressBar.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

export const StepProgressBar = ({ steps = [], currentStep }) => {
  const renderStepProgressBar = () => {
    const renderIcon = step => {
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

    const renderSteps = () => {
      const getIconClassName = step => {
        if (step.idx < currentStep) {
          return styles.activeCompleted;
        } else if (step.idx === currentStep) {
          if (step.isRunning) {
            return styles.activeIncompleted;
          } else {
            return styles.activeCompleted;
          }
        } else {
          return styles.inactive;
        }
      };
      const getStepLabel = step => {
        if (!step.isRunning) {
          return step.labelCompleted;
        } else {
          if (step.idx === currentStep) {
            return step.labelRunning;
          } else {
            return step.labelUndone;
          }
        }
      };
      return steps.map((step, index) => {
        return (
          <li className={styles.step} key={uniqueId('step_')}>
            {
              <div className={`${styles.iconWrapper} ${getIconClassName(step)}`}>
                <FontAwesomeIcon
                  className={step.idx === currentStep && step.isRunning ? 'fa-spin' : ''}
                  icon={renderIcon(step)}
                />
              </div>
            }
            {<label>{getStepLabel(step)}</label>}
          </li>
        );
      });
    };

    if (!isEmpty(steps)) {
      return (
        <div className={styles.stepsWrapper}>
          <ul className={`${styles.stepList}`}>{renderSteps()}</ul>
        </div>
      );
      //   <div className={styles.stepsWrapper} key={uniqueId('step_')}>
      //     <FontAwesomeIcon className={styles.step} icon={AwesomeIcons(renderIcon(step))} />
      //     <label>{step.label}</label>
      //   </div>
    }
  };

  return renderStepProgressBar();
};
