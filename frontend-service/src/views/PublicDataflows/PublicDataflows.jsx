import { useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useRecoilValue } from 'recoil';

import isEmpty from 'lodash/isEmpty';

import { config } from 'conf';
import { routes } from 'conf/routes';

import ReactTooltip from 'react-tooltip';

import styles from './PublicDataflows.module.scss';

import { Filters } from 'views/_components/Filters';
import { InputText } from 'views/_components/InputText';
import { Paginator } from 'views/_components/DataTable/_components/Paginator';
import { PublicCard } from 'views/_components/PublicCard';
import { PublicLayout } from 'views/_components/Layout/PublicLayout';
import { Spinner } from 'views/_components/Spinner';

import { ThemeContext } from 'views/_functions/Contexts/ThemeContext';

import { DataflowService } from 'services/DataflowService';

import { filterByCustomFilterStore } from 'views/_components/Filters/_functions/Stores/filterStore';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { useBreadCrumbs } from 'views/_functions/Hooks/useBreadCrumbs';
import { useApplyFilters } from 'views/_functions/Hooks/useApplyFilters';

import { CurrentPage } from 'views/_functions/Utils';
import { getUrl } from 'repositories/_utils/UrlUtils';
import { PaginatorRecordsCount } from 'views/_components/DataTable/_functions/Utils/PaginatorRecordsCount';

export const PublicDataflows = () => {
  const navigate = useNavigate();

  const resourcesContext = useContext(ResourcesContext);
  const themeContext = useContext(ThemeContext);

  const filterBy = useRecoilValue(filterByCustomFilterStore('publicDataflows'));

  const { setData, sortByOptions } = useApplyFilters('publicDataflows');

  const [contentStyles, setContentStyles] = useState({});
  const [filteredRecords, setFilteredRecords] = useState(0);
  const [goToPage, setGoToPage] = useState(1);
  const [isFiltered, setIsFiltered] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [pageInputTooltip, setPageInputTooltip] = useState(resourcesContext.messages['currentPageInfoMessage']);
  const [pagination, setPagination] = useState({ firstRow: 0, numberRows: config.DATAFLOWS_PER_PAGE, pageNum: 0 });
  const [publicDataflows, setPublicDataflows] = useState([]);
  const [totalRecords, setTotalRecords] = useState(0);

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
            {filteredRecords > 0 ? `${resourcesContext.messages['of']} ${Math.ceil(filteredRecords / numberRows)}` : 1}
          </label>
        </span>
      );
    }
  };

  const filterOptions = [
    {
      nestedOptions: [
        { key: 'name', label: resourcesContext.messages['name'], isSortable: true },
        { key: 'description', label: resourcesContext.messages['description'], isSortable: true },
        { key: 'legalInstrument', label: resourcesContext.messages['legalInstrument'], isSortable: true },
        { key: 'obligationTitle', label: resourcesContext.messages['obligation'], isSortable: true },
        { key: 'obligationId', label: resourcesContext.messages['obligationId'], isSortable: true, keyfilter: 'num' }
      ],
      type: 'INPUT'
    },
    {
      key: 'status',
      label: resourcesContext.messages['status'],
      isSortable: true,
      template: 'LevelError',
      dropdownOptions: [
        { label: resourcesContext.messages['design'].toUpperCase(), value: config.dataflowStatus.DESIGN },
        { label: resourcesContext.messages['open'].toUpperCase(), value: config.dataflowStatus.OPEN_FE },
        { label: resourcesContext.messages['closed'].toUpperCase(), value: config.dataflowStatus.CLOSED }
      ],
      type: 'DROPDOWN'
    },
    {
      key: 'expirationDate',
      label: resourcesContext.messages['expirationDateFilterLabel'],
      isSortable: true,
      type: 'DATE'
    }
  ];

  const onChangeCurrentPage = event => {
    if (event.key === 'Enter' && goToPage !== '' && goToPage !== firstRow + 1) {
      const pc = Math.ceil(filteredRecords / pagination.numberRows) || 1;
      const p = Math.floor(event.target.value - 1);

      if (p >= 0 && p < pc) {
        const newPageState = {
          firstRow: (event.target.value - 1) * numberRows,
          numberRows: numberRows,
          pageNum: p
        };
        setPagination(newPageState);
      }
    } else {
      setGoToPage(event.target.value);
      if (event.target.value <= 0 || event.target.value > Math.ceil(filteredRecords / numberRows)) {
        setPageInputTooltip(
          `${resourcesContext.messages['currentPageErrorMessage']} ${Math.ceil(filteredRecords / numberRows)}`
        );
      } else {
        setPageInputTooltip(resourcesContext.messages['currentPageInfoMessage']);
      }
    }
  };

  const onLoadPublicDataflows = async (sortBy = sortByOptions) => {
    setIsLoading(true);

    try {
      const publicData = await DataflowService.getPublicData({ filterBy, numberRows, pageNum, sortByOptions: sortBy });

      setPublicDataflows(publicData.dataflows);
      setData(
        publicData.dataflows.map(dataflow => ({
          ...dataflow,
          legalInstrument: dataflow.obligation.legalInstrument?.alias,
          obligationId: dataflow.obligation?.obligationId,
          obligationTitle: dataflow.obligation?.title
        }))
      );
      setFilteredRecords(publicData.filteredRecords);
      setTotalRecords(publicData.totalRecords);
      setIsFiltered(publicData.filteredRecords !== publicData.totalRecords);
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

  const renderPaginator = () => {
    if (totalRecords > 0) {
      return (
        <Paginator
          areComponentsVisible={filteredRecords > config.DATAFLOWS_PER_PAGE}
          className={`p-paginator-bottom ${styles.paginator}`}
          first={firstRow}
          onPageChange={onPaginate}
          rightContent={
            <PaginatorRecordsCount
              dataLength={totalRecords}
              filteredDataLength={filteredRecords}
              isFiltered={isFiltered}
              nameRecords="dataflows"
            />
          }
          rows={numberRows}
          rowsPerPageOptions={[100, 150, 200]}
          template={currentPageTemplate}
          totalRecords={filteredRecords}
        />
      );
    }
  };

  const renderPublicDataflowsContent = () => {
    if (isLoading) {
      return (
        <div className={styles.noDataflows}>
          <Spinner className={styles.spinner} />
        </div>
      );
    }

    if (isEmpty(publicDataflows)) {
      if (totalRecords !== filteredRecords) {
        return (
          <div className={styles.noDataflows}>{resourcesContext.messages['noDataflowsWithSelectedParameters']}</div>
        );
      } else {
        return <div className={styles.noDataflows}>{resourcesContext.messages['noDataflows']}</div>;
      }
    }

    return (
      <div className="responsiveCardsGrid">
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
    );
  };

  return (
    <PublicLayout>
      <div className={styles.content} style={contentStyles}>
        <div className={`rep-container ${styles.repContainer}`}>
          <h1 className={styles.title}>{resourcesContext.messages['dataflows']}</h1>
          <Filters
            className="publicDataflows"
            isLoading={isLoading}
            onFilter={() => setPagination({ firstRow: 0, numberRows: numberRows, pageNum: 0 })}
            onReset={() => setPagination({ firstRow: 0, numberRows: numberRows, pageNum: 0 })}
            onSort={() => setPagination({ firstRow: 0, numberRows: numberRows, pageNum: 0 })}
            options={filterOptions}
            panelClassName="overwriteZindexPanel"
            recoilId="publicDataflows"
          />
          <div className={styles.topPaginator}>{renderPaginator()}</div>
          {renderPublicDataflowsContent()}
          <div className={styles.bottomPaginator}>{renderPaginator()}</div>
        </div>
      </div>
    </PublicLayout>
  );
};
