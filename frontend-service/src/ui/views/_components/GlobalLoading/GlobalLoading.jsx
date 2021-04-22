import { useContext } from 'react';

import styles from './GlobalLoading.module.css';

import { LoadingContext } from 'ui/views/_functions/Contexts/LoadingContext';
import { Spinner } from 'ui/views/_components/Spinner';

export const GlobalLoading = () => {
  const { loadingCount } = useContext(LoadingContext);
  return (
    <>
      {loadingCount > 0 && (
        <div className={styles.spinnerWrap}>
          <Spinner className={styles.spinner} />
        </div>
      )}
    </>
  );
};
