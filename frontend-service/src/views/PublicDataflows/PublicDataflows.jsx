import { Fragment, useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import { config } from 'conf';
import { routes } from 'conf/routes';

import styles from './PublicDataflows.module.scss';

import { InputText } from 'views/_components/InputText';
import { Paginator } from 'views/_components/DataTable/_components/Paginator';
import { PublicCard } from 'views/_components/PublicCard';
import ReactTooltip from 'react-tooltip';
import { Spinner } from 'views/_components/Spinner';
import { PublicLayout } from 'views/_components/Layout/PublicLayout';

import { ThemeContext } from 'views/_functions/Contexts/ThemeContext';

import { DataflowService } from 'services/DataflowService';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { useBreadCrumbs } from 'views/_functions/Hooks/useBreadCrumbs';

import { CurrentPage } from 'views/_functions/Utils';
import { getUrl } from 'repositories/_utils/UrlUtils';

export const PublicDataflows = () => {
  const navigate = useNavigate();

  const resourcesContext = useContext(ResourcesContext);
  const themeContext = useContext(ThemeContext);

  const [contentStyles, setContentStyles] = useState({});
  const [isLoading, setIsLoading] = useState(true);
  const [pagination, setPagination] = useState({ firstRow: 0, numberRows: 10, pageNum: 0 });
  const [publicDataflows, setPublicDataflows] = useState([]);
  const [goToPage, setGoToPage] = useState(1);
  const [pageInputTooltip, setPageInputTooltip] = useState(resourcesContext.messages['currentPageInfoMessage']);

  useBreadCrumbs({ currentPage: CurrentPage.PUBLIC_DATAFLOWS });

  useEffect(() => {
    onLoadPublicDataflows();
  }, [pagination]);

  useEffect(() => {
    if (!themeContext.headerCollapse) {
      setContentStyles({ marginTop: `${config.theme.cookieConsentHeight + 6}px` });
    } else {
      setContentStyles({});
    }
  }, [themeContext.headerCollapse]);

  const { firstRow, numberRows, pageNum } = pagination;

  const currentPageTemplate = {
    layout: 'FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport',
    // eslint-disable-next-line react/no-multi-comp
    CurrentPageReport: options => {
      return (
        <span className={styles.currentPageWrapper}>
          <label className={styles.currentPageLabel}>{resourcesContext.messages['goTo']}</label>
          <InputText
            className={styles.currentPageInput}
            data-for="pageInputTooltip"
            data-tip
            id="currentPageInput"
            keyfilter="pint"
            onChange={onChangeCurrentPage}
            onKeyDown={onChangeCurrentPage}
            style={{
              border: (goToPage <= 0 || goToPage > options.totalPages) && '1px solid var(--errors)',
              boxShadow:
                goToPage <= 0 || goToPage > options.totalPages ? 'var(--inputtext-box-shadow-focus-error)' : 'none'
            }}
            value={goToPage}
          />
          <ReactTooltip border={true} effect="solid" id="pageInputTooltip" place="bottom">
            {pageInputTooltip}
          </ReactTooltip>
          <label className={styles.currentPageOf}>
            {publicDataflows.length > 0
              ? `${resourcesContext.messages['of']} ${Math.ceil(publicDataflows.length / numberRows)}`
              : 1}
          </label>
        </span>
      );
    }
  };

  const onChangeCurrentPage = event => {
    if (event.key === 'Enter' && goToPage !== '' && goToPage !== firstRow + 1) {
      var pc = Math.ceil(publicDataflows.length / pagination.numberRows) || 1;
      var p = Math.floor(event.target.value - 1);

      if (p >= 0 && p < pc) {
        var newPageState = {
          firstRow: (event.target.value - 1) * numberRows,
          numberRows: numberRows,
          pageNum: p
        };
        setPagination(newPageState);
      }
    } else {
      setGoToPage(event.target.value);
      if (event.target.value <= 0 || event.target.value > Math.ceil(publicDataflows.length / numberRows)) {
        setPageInputTooltip(
          `${resourcesContext.messages['currentPageErrorMessage']} ${Math.ceil(publicDataflows.length / numberRows)}`
        );
      } else {
        setPageInputTooltip(resourcesContext.messages['currentPageInfoMessage']);
      }
    }
  };

  const onLoadPublicDataflows = async () => {
    console.log('pagination :>> ', pagination);
    setIsLoading(true);
    try {
      // ADD PAGENUM AND NUMBERROWS AND INTEGRATE WITH FILTERS
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

  const onPaginate = event => {
    setGoToPage(event.page + 1);
    setPagination({ firstRow: event.first, numberRows: event.rows, pageNum: event.page });
  };

  const renderPaginatorRecordsCount = () => {
    // const renderFilteredRowsLabel = () => {
    //   if (isFiltered && publicDataflows.length !== filteredData.length) {
    //     return `${resourcesContext.messages['filtered']}: ${filteredData.length} | `;
    //   }
    // };

    // const renderTotalRecordsLabel = () =>
    //   `${resourcesContext.messages['totalRecords']} ${publicDataflows.length} ${' '} ${resourcesContext.messages[
    //     'records'
    //   ].toLowerCase()}`;

    // const renderFilteredLabel = () => {
    //   if (isFiltered && publicDataflows.length === filteredData.length) {
    //     return ` (${resourcesContext.messages['filtered'].toLowerCase()})`;
    //   }
    // };

    // return (
    //   <Fragment>
    //     {renderFilteredRowsLabel()}
    //     {renderTotalRecordsLabel()}
    //     {renderFilteredLabel()}
    //   </Fragment>
    // );

    return (
      <Fragment>
        {resourcesContext.messages['totalRecords']} {publicDataflows.length}{' '}
        {resourcesContext.messages['records'].toLowerCase()}
      </Fragment>
    );
  };

  const renderPaginator = () => {
    // ADD IF THERE ARE DATAFLOWS TO RENDER PAGINATOR
    if (!isLoading) {
      return (
        <Paginator
          className="p-paginator-bottom"
          first={firstRow}
          onPageChange={onPaginate}
          rightContent={renderPaginatorRecordsCount()}
          rows={numberRows}
          rowsPerPageOptions={[5, 10, 15]}
          template={currentPageTemplate}
          totalRecords={publicDataflows.length}
        />
      );
    }
  };

  const renderPublicDataflowsContent = () => {
    if (isLoading) {
      return <Spinner style={{ left: 0 }} />;
    }

    if (publicDataflows.length === 0) {
      return <div className={styles.noDataflows}>{resourcesContext.messages['noDataflows']}</div>;
    }

    return (
      <div>
        <div className={styles.topPaginator}>{renderPaginator()}</div>
        <div className={styles.dataflowsList}>
          {publicDataflows.map(dataflow => (
            <PublicCard
              animation
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
          ))}
        </div>
        <div className={styles.bottomPaginator}>{renderPaginator()}</div>
      </div>
    );
  };

  return (
    <PublicLayout>
      <div className={styles.content} style={contentStyles}>
        <div className={`rep-container ${styles.repContainer}`}>
          <h1 className={styles.title}>{resourcesContext.messages['dataflows']}</h1>
          <div className={styles.dataflowsList}>{renderPublicDataflowsContent()}</div>
        </div>
      </div>
    </PublicLayout>
  );
};
