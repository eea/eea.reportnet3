import { useState, useEffect } from 'react';

import styles from './GoTopButton.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

const GoTopButton = ({ parentRef, referenceMargin }) => {
  const [isVisible, setIsVisible] = useState(false);

  const callbackFunction = entries => {
    const [entry] = entries;
    setIsVisible(!entry.isIntersecting);
  };

  const options = {
    root: null,
    rootMargin: `-${referenceMargin}px`,
    threshold: 0
  };

  useEffect(() => {
    const observer = new IntersectionObserver(callbackFunction, options);
    if (parentRef?.current) observer.observe(parentRef.current);

    return () => {
      if (parentRef?.current) observer.unobserve(parentRef.current);
      observer.disconnect();
    };
  }, [parentRef, options]);

  if (!isVisible) {
    return null;
  }

  return (
    <div className={styles.goTopButton} onClick={() => window.scrollTo(0, 0)}>
      <FontAwesomeIcon icon={AwesomeIcons('angleUp')} />
    </div>
  );
};

export { GoTopButton };
