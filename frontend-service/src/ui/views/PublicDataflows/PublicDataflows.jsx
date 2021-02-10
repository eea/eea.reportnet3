import React, { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import { config } from 'conf';

import styles from './PublicDataflows.module.scss';

import { PublicCard } from 'ui/views/_components/PublicCard';
import { PublicLayout } from 'ui/views/_components/Layout/PublicLayout';

import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';

import { DataflowService } from 'core/services/Dataflow';

export const PublicDataflows = () => {
  const themeContext = useContext(ThemeContext);

  const [contentStyles, setContentStyles] = useState({});
  const [publicDataflows, setPublicDataflows] = useState([]);

  useEffect(() => {
    onLoadPublicDataflows();
  }, []);

  useEffect(() => {
    if (!themeContext.headerCollapse) {
      setContentStyles({ marginTop: `${config.theme.cookieConsentHeight + 6}px` });
    } else {
      setContentStyles({});
    }
  }, [themeContext.headerCollapse]);

  const onLoadPublicDataflows = async () => {
    const publicData = await DataflowService.publicData();
    setPublicDataflows(publicData);
  };

  return (
    <PublicLayout>
      <div style={contentStyles}>
        <h3>Public dataflows:</h3>
        <div className={styles.dataflowsList}>
          {!isEmpty(publicDataflows) ? (
            publicDataflows.map(dataflow => <PublicCard card={dataflow} onCardClick={() => {}} />)
          ) : (
            <span>No public dataflows available</span>
          )}
        </div>
      </div>
    </PublicLayout>
  );
};
