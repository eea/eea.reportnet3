import { useContext } from 'react';

import styles from './GlobalLoading.module.css';

import { LoadingContext } from 'views/_functions/Contexts/LoadingContext';
import { Spinner } from 'views/_components/Spinner';

export const GlobalLoading = () => {
  const { loadingCount } = useContext(LoadingContext);

  const renderSpinner = () => {
    if (loadingCount > 0) {
      return (
        <div className={styles.spinnerWrap}>
          <Spinner className={styles.spinner} />
        </div>
      );
    }
  };

  return <>{renderSpinner()}</>;
};
