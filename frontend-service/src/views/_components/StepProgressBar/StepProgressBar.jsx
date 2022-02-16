import { memo } from 'react';
import isEmpty from 'lodash/isEmpty';
import uniqueId from 'lodash/uniqueId';

import styles from './StepProgressBar.module.scss';

import { Step } from './_components/Step';

export const StepProgressBar = memo(({ className = '', steps = [], currentStep }) => {
  const renderStepProgressBar = () => {
    const renderSteps = () => steps.map(step => <Step currentStep={currentStep} key={uniqueId('step_')} step={step} />);

    if (!isEmpty(steps)) {
      return (
        <div className={`${className} ${styles.stepsWrapper}`}>
          <ul className={`${styles.stepList}`}>{renderSteps()}</ul>
        </div>
      );
    }
  };

  return renderStepProgressBar();
});
