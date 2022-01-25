import { useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';

import { config } from 'conf';
import { routes } from 'conf/routes';

import styles from './PublicDataflows.module.scss';

import { MyFilters } from 'views/_components/MyFilters';
import { PublicCard } from 'views/_components/PublicCard';
import { PublicLayout } from 'views/_components/Layout/PublicLayout';
import { Spinner } from 'views/_components/Spinner';

import { DataflowService } from 'services/DataflowService';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { ThemeContext } from 'views/_functions/Contexts/ThemeContext';

import { useBreadCrumbs } from 'views/_functions/Hooks/useBreadCrumbs';
import { useFilters } from 'views/_functions/Hooks/useFilters';

import { CurrentPage } from 'views/_functions/Utils';
import { getUrl } from 'repositories/_utils/UrlUtils';

export const PublicDataflows = () => {
  const navigate = useNavigate();

  const resourcesContext = useContext(ResourcesContext);
  const themeContext = useContext(ThemeContext);

  const [contentStyles, setContentStyles] = useState({});
  const [isLoading, setIsLoading] = useState(true);
  const [publicDataflows, setPublicDataflows] = useState([]);

  const { filteredData, isFiltered } = useFilters('publicDataflows');

  useBreadCrumbs({ currentPage: CurrentPage.PUBLIC_DATAFLOWS });

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
    try {
      const publicData = await DataflowService.getPublicData();
      setPublicDataflows(publicData);
    } catch (error) {
      console.error('PublicDataflows - onLoadPublicDataflows.', error);
    } finally {
      setIsLoading(false);
    }
  };

  const onOpenDataflow = (dataflowId, newTab) => {
    const url = getUrl(routes.PUBLIC_DATAFLOW_INFORMATION, { dataflowId }, true);
    if (!newTab) {
      return navigate(url);
    } else {
      window.open(url, '_blank');
    }
  };

  const options = [
    {
      nestedOptions: [
        { key: 'name', label: resourcesContext.messages['name'], isSortable: true },
        { key: 'description', label: resourcesContext.messages['description'], isSortable: true },
        { key: 'legalInstrument', label: resourcesContext.messages['legalInstrument'], isSortable: true },
        { key: 'obligationTitle', label: resourcesContext.messages['obligation'], isSortable: true },
        { key: 'obligationId', label: resourcesContext.messages['obligationId'], isSortable: true }
      ],
      type: 'INPUT'
    }
  ];

  console.log('filteredData :>> ', filteredData);

  const renderDataflows = () => {
    if (isLoading) {
      return <Spinner style={{ left: 0 }} />;
    }

    if (isEmpty(filteredData)) {
      return (
        <div className={styles.noDataflows}>
          {isFiltered
            ? resourcesContext.messages['noDataflowsWithSelectedParameters']
            : resourcesContext.messages['noDataflows']}
        </div>
      );
    }

    return filteredData.map(dataflow => (
      <PublicCard
        animation={true}
        card={dataflow}
        dataflowId={dataflow.id}
        dueDate={dataflow.expirationDate}
        key={dataflow.id}
        landingPageCard={false}
        obligation={dataflow.obligation}
        onCardClick={onOpenDataflow}
        status={resourcesContext.messages[dataflow.status]}
        subtitle={{ text: dataflow.description, url: '' }}
        title={{ text: dataflow.name, url: '' }}
      />
    ));
  };

  return (
    <PublicLayout>
      <div className={styles.content} style={contentStyles}>
        <div className={`rep-container ${styles.repContainer}`}>
          <h1 className={styles.title}>Dataflows</h1>
          <MyFilters data={publicDataflows} options={options} viewType="publicDataflows" />
          <div className={styles.dataflowsList}>{renderDataflows()}</div>
        </div>
      </div>
    </PublicLayout>
  );
};
