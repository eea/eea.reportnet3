import { Fragment, useContext, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useRecoilValue } from 'recoil';

import ReactTooltip from 'react-tooltip';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import capitalize from 'lodash/capitalize';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';
import { routes } from 'conf/routes';

import styles from './PublicCountryInformation.module.scss';

import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { DownloadFile } from 'views/_components/DownloadFile';
import { MyFilters } from 'views/_components/MyFilters';
import { PublicLayout } from 'views/_components/Layout/PublicLayout';
import { Spinner } from 'views/_components/Spinner';
import { Title } from 'views/_components/Title';

import { DataflowService } from 'services/DataflowService';
import { DatasetService } from 'services/DatasetService';

import { filterByState } from 'views/_components/MyFilters/_functions/Stores/filtersStores';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { ThemeContext } from 'views/_functions/Contexts/ThemeContext';

import { useBreadCrumbs } from 'views/_functions/Hooks/useBreadCrumbs';
import { useApplyFilters } from 'views/_functions/Hooks/useApplyFilters';
import { useFilters } from 'views/_functions/Hooks/useFilters';

import { CurrentPage } from 'views/_functions/Utils';
import { DataflowUtils } from 'services/_utils/DataflowUtils';
import { getUrl } from 'repositories/_utils/UrlUtils';

export const PublicCountryInformation = () => {
  const { countryCode } = useParams();

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const themeContext = useContext(ThemeContext);

  const baseRod3Url = 'https://rod.eionet.europa.eu';

  const [contentStyles, setContentStyles] = useState({});
  const [countryName, setCountryName] = useState('');
  const [dataflows, setDataflows] = useState([]);
  const [filteredRecords, setFilteredRecords] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [isReset, setIsReset] = useState(false);
  const [pagination, setPagination] = useState({ firstRow: 0, numberRows: 10, pageNum: 0 });
  const [sortField, setSortField] = useState('');
  const [sortOrder, setSortOrder] = useState(0);
  const [totalRecords, setTotalRecords] = useState(0);

  const filterBy = useRecoilValue(filterByState('publicCountryInformation'));

  const { isFiltered: areFiltersFilled } = useApplyFilters('publicCountryInformation');

  useBreadCrumbs({ currentPage: CurrentPage.PUBLIC_COUNTRY, countryCode });

  useEffect(() => {
    onLoadPublicCountryInformation();
  }, [pagination, sortOrder, sortField, isReset]);

  useEffect(() => {
    !isNil(countryCode) && getCountryName();
  }, [countryCode]);

  useEffect(() => {
    if (!themeContext.headerCollapse) {
      setContentStyles({ marginTop: `${config.theme.cookieConsentHeight + 6}px` });
    } else {
      setContentStyles({});
    }
  }, [themeContext.headerCollapse]);

  const { firstRow, numberRows, pageNum } = pagination;

  const { isFiltered } = useFilters('publicCountryInformation');

  const getCountryName = () => {
    if (!isNil(config.countriesByGroup)) {
      const allCountries = config.countriesByGroup['eeaCountries']
        .concat(config.countriesByGroup['cooperatingCountries'])
        .concat(config.countriesByGroup['otherCountries']);
      allCountries.forEach(country => {
        if (countryCode === country.code) {
          setCountryName(country.name);
        }
      });
    }
  };

  const getDeliveryStatus = (dataflow, dataset) => {
    if (!dataset?.isReleased) {
      return config.datasetStatus.PENDING.label;
    } else {
      if (!dataflow.manualAcceptance) {
        return config.datasetStatus.DELIVERED.label;
      } else {
        return DataflowUtils.getTechnicalAcceptanceStatus(dataflow.datasets.map(dataset => dataset.status));
      }
    }
  };

  const onChangePage = event =>
    setPagination({ firstRow: event.first, numberRows: event.rows, pageNum: Math.floor(event.first / event.rows) });

  const onFileDownload = async (dataflowId, dataProviderId, fileName) => {
    try {
      let fileContent;

      if (!isNil(dataProviderId)) {
        fileContent = await DatasetService.downloadPublicDatasetFile(dataflowId, dataProviderId, fileName);
      } else {
        fileContent = await DatasetService.downloadPublicReferenceDatasetFileData(dataflowId, fileName);
      }
      DownloadFile(fileContent.data, fileName);
    } catch (error) {
      console.error('PublicCountryInformation - onFileDownload.', error);
      if (error.response.status === 404) {
        notificationContext.add({ type: 'DOWNLOAD_DATASET_FILE_NOT_FOUND_EVENT' }, true);
      } else {
        notificationContext.add({ type: 'DOWNLOAD_DATASET_FILE_ERROR' }, true);
      }
    }
  };

  const getSortOrder = () => {
    if (sortOrder === -1) {
      return 0;
    } else if (isNil(sortOrder)) {
      return undefined;
    } else {
      return sortOrder;
    }
  };

  const onLoadPublicCountryInformation = async () => {
    setIsLoading(true);
    try {
      const data = await DataflowService.getPublicDataflowsByCountryCode({
        countryCode,
        sortOrder: getSortOrder(),
        pageNum,
        numberRows,
        sortField,
        filterBy
      });
      setTotalRecords(data.totalRecords);
      setPublicInformation(data.dataflows);
      setFilteredRecords(data.filteredRecords);
      // setIsFiltered(data.filteredRecords !== data.totalRecords);
      setIsReset(false);
    } catch (error) {
      console.error('PublicCountryInformation - onLoadPublicCountryInformation.', error);
      notificationContext.add({ type: 'LOAD_DATAFLOWS_BY_COUNTRY_ERROR' }, true);
    } finally {
      setIsLoading(false);
    }
  };

  const onSort = event => {
    setSortOrder(event.sortOrder);
    setSortField(event.sortField);
  };

  const setPublicInformation = dataflows => {
    if (isNil(dataflows)) return [];

    const publicDataflows = dataflows
      .filter(dataflow => !isNil(dataflow.datasets))
      .map(dataflow => {
        const dataset = dataflow.datasets[0];

        const publicFileNames = dataflow.datasets
          ?.filter(dataset => !isNil(dataset.publicFileName))
          .map(dataset => ({ dataProviderId: dataset.dataProviderId, fileName: dataset.publicFileName }));

        const referencePublicFileNames = dataflow.referenceDatasets
          ?.filter(referenceDataset => !isNil(referenceDataset.publicFileName))
          .map(referenceDataset => ({ fileName: referenceDataset.publicFileName }));

        return {
          deadline: dataflow.expirationDate,
          id: dataflow.id,
          legalInstrumentAlias: dataflow.obligation?.legalInstrument.alias,
          legalInstrumentId: dataflow.obligation?.legalInstrument.id,
          legalInstrument: dataflow.obligation?.legalInstrument.title,
          name: dataflow.name,
          obligationId: dataflow.obligation.obligationId,
          obligation: dataflow.obligation.title,
          publicFilesNames: publicFileNames,
          referencePublicFilesNames: referencePublicFileNames,
          deliveryDate: !isNil(dataset) ? dataset?.releaseDate : '-',
          deliveryStatus: getDeliveryStatus(dataflow, dataset).toUpperCase(),
          restrictFromPublic: dataflow.datasets ? dataflow.datasets[0]?.restrictFromPublic : false,
          status: resourcesContext.messages[dataflow.status].toUpperCase()
        };
      });
    setDataflows(publicDataflows);
  };

  const renderTableColumns = () => {
    const columns = [
      { key: 'name', header: resourcesContext.messages['name'], template: renderDataflowNameBodyColumn },
      { key: 'obligation', header: resourcesContext.messages['obligation'], template: renderObligationBodyColumn },
      {
        key: 'legalInstrument',
        header: resourcesContext.messages['legalInstrument'],
        template: renderLegalInstrumentBodyColumn
      },
      { key: 'deadline', header: resourcesContext.messages['deadline'] },
      { key: 'status', header: resourcesContext.messages['status'], template: renderStatusBodyColumn },
      { key: 'deliveryDate', header: resourcesContext.messages['deliveryDate'] },
      {
        key: 'deliveryStatus',
        header: resourcesContext.messages['deliveryStatus'],
        template: renderDeliveryStatusBodyColumn
      },
      {
        key: 'referencePublicFilesNames',
        header: resourcesContext.messages['referenceDatasets'],
        template: renderDownloadReferenceFileBodyColumn
      },
      {
        key: 'publicFilesNames',
        header: resourcesContext.messages['files'],
        template: renderDownloadFileBodyColumn
      }
    ];

    return columns.map(column => (
      <Column
        body={column.template}
        className={column.className ? column.className : ''}
        columnResizeMode="expand"
        editor={column.editor}
        field={column.key}
        header={column.header}
        key={column.key}
        rowEditor={column.key === 'actions'}
        sortable={column.key === 'publicFilesNames' || column.key === 'referencePublicFilesNames' ? false : true}
      />
    ));
  };

  const renderDownloadFileBodyColumn = rowData => {
    if (!rowData.restrictFromPublic) {
      return (
        <div className={styles.filesContainer}>
          {rowData.publicFilesNames.map(publicFileName => (
            <span
              className={styles.filesIcon}
              key={publicFileName.fileName}
              onClick={() => onFileDownload(rowData.id, publicFileName.dataProviderId, publicFileName.fileName)}>
              <FontAwesomeIcon
                className={styles.cursorPointer}
                data-for={publicFileName.fileName}
                data-tip
                icon={AwesomeIcons('7z')}
                role="button"
              />
              <ReactTooltip
                border={true}
                className={styles.tooltipClass}
                effect="solid"
                id={publicFileName.fileName}
                place="top">
                <span>{publicFileName.fileName.split('-')[1]}</span>
              </ReactTooltip>
            </span>
          ))}
        </div>
      );
    } else {
      return (
        <div className={styles.filesContainer}>
          <FontAwesomeIcon
            className={styles.restrictFromPublicIcon}
            data-for={'restrictFromPublicField'}
            data-tip
            icon={AwesomeIcons('lock')}
          />
          <ReactTooltip
            border={true}
            className={styles.tooltipClass}
            effect="solid"
            id={'restrictFromPublicField'}
            place="top">
            <span>{resourcesContext.messages['restrictFromPublicField']}</span>
          </ReactTooltip>
        </div>
      );
    }
  };

  const renderDownloadReferenceFileBodyColumn = rowData => {
    if (rowData.referencePublicFilesNames.length !== 0) {
      return (
        <div className={styles.filesContainer}>
          {rowData.referencePublicFilesNames.map(referencePublicFilesName => (
            <span
              className={styles.filesIcon}
              key={referencePublicFilesName.fileName}
              onClick={() => onFileDownload(rowData.id, null, referencePublicFilesName.fileName)}>
              <FontAwesomeIcon
                className={styles.cursorPointer}
                data-for={referencePublicFilesName.fileName}
                data-tip
                icon={AwesomeIcons('7z')}
                role="button"
              />
              <ReactTooltip
                border={true}
                className={styles.tooltipClass}
                effect="solid"
                id={referencePublicFilesName.fileName}
                place="top">
                <span>{referencePublicFilesName.fileName}</span>
              </ReactTooltip>
            </span>
          ))}
        </div>
      );
    }
  };

  const filterOptions = [
    {
      type: 'INPUT',
      nestedOptions: [
        { key: 'name', label: resourcesContext.messages['name'] },
        { key: 'obligation', label: resourcesContext.messages['obligation'] },
        { key: 'legalInstrument', label: resourcesContext.messages['legalInstrument'] }
      ]
    },
    { type: 'DATE', key: 'deadline', label: resourcesContext.messages['deadline'] },
    {
      key: 'status',
      label: resourcesContext.messages['status'],
      template: 'LevelError',
      dropdownOptions: [
        { label: resourcesContext.messages['design'].toUpperCase(), value: config.dataflowStatus.DESIGN },
        { label: resourcesContext.messages['open'].toUpperCase(), value: config.dataflowStatus.OPEN_FE },
        { label: resourcesContext.messages['closed'].toUpperCase(), value: config.dataflowStatus.CLOSED }
      ],
      type: 'DROPDOWN'
    },
    { type: 'DATE', key: 'deliveryDate', label: resourcesContext.messages['deliveryDate'] },
    {
      type: 'MULTI_SELECT',
      key: 'deliveryStatus',
      label: resourcesContext.messages['deliveryStatus']
    }
  ];

  const renderFilters = () => {
    return (
      <MyFilters
        className="publicCountryInformationFilters"
        data={dataflows}
        onFilter={() => {
          if (areFiltersFilled) {
            setPagination({ firstRow: 0, numberRows: 100, pageNum: 0 });
          } else {
            onLoadPublicCountryInformation();
          }
        }}
        onReset={setIsReset}
        options={filterOptions}
        viewType="publicCountryInformation"
      />
    );
  };

  const renderDataflowNameBodyColumn = rowData => (
    <div onClick={e => e.stopPropagation()}>
      {rowData.obligationId
        ? renderRedirectText(rowData.name, getUrl(routes.PUBLIC_DATAFLOW_INFORMATION, { dataflowId: rowData.id }, true))
        : rowData.name}
    </div>
  );

  const renderDeliveryStatusBodyColumn = rowData => <div>{capitalize(rowData.deliveryStatus)}</div>;

  const renderLegalInstrumentBodyColumn = rowData => (
    <div onClick={e => e.stopPropagation()}>
      {rowData.legalInstrumentId
        ? renderRedirectText(rowData.legalInstrumentAlias, `${baseRod3Url}/instruments/${rowData.legalInstrumentId}`)
        : rowData.legalInstrumentAlias}
    </div>
  );

  const renderObligationBodyColumn = rowData => (
    <div onClick={e => e.stopPropagation()}>
      {rowData.obligationId
        ? renderRedirectText(rowData.obligation, `${baseRod3Url}/obligations/${rowData.obligationId}`)
        : rowData.obligation}
    </div>
  );

  const renderRedirectText = (text, url) => (
    <span>
      {text}{' '}
      <a href={url} rel="noopener noreferrer" target="_blank" title={text}>
        <FontAwesomeIcon
          aria-hidden={false}
          aria-label={text}
          className="p-breadcrumb-home"
          icon={AwesomeIcons('externalUrl')}
          role="button"
        />
      </a>
    </span>
  );

  console.log('isFiltered :>> ', isFiltered);
  const renderPaginatorRecordsCount = () => (
    <Fragment>
      {isFiltered ? `${resourcesContext.messages['filtered']}: ${filteredRecords} | ` : ''}
      {`${resourcesContext.messages['totalRecords']} ${totalRecords} ${' '} ${resourcesContext.messages[
        'records'
      ].toLowerCase()}`}
    </Fragment>
  );

  const renderPublicCountryInformationTitle = () => {
    if (!isEmpty(countryName)) {
      return (
        <Title icon="clone" iconSize={'4rem'} subtitle={resourcesContext.messages['dataflows']} title={countryName} />
      );
    }
  };

  const renderPublicCountryInformation = () => {
    if (isLoading) {
      return <Spinner className={styles.isLoading} />;
    }

    if (isEmpty(countryName)) {
      return <div className={styles.noDataflows}>{resourcesContext.messages['wrongUrlCountryCode']}</div>;
    }

    if (isEmpty(dataflows) && !isFiltered) {
      return <div className={styles.noDataflows}>{resourcesContext.messages['noDataflows']}</div>;
    }

    if (isEmpty(dataflows) && isFiltered) {
      return <div className={styles.noDataflows}>{resourcesContext.messages['dataflowsNotMatchingFilter']}</div>;
    }

    return (
      <DataTable
        areComponentsVisible={filteredRecords > config.DATAFLOWS_PER_PAGE}
        autoLayout={true}
        className={styles.countriesList}
        first={firstRow}
        lazy={true}
        onPage={onChangePage}
        onSort={onSort}
        paginator={true}
        paginatorRight={renderPaginatorRecordsCount()}
        rows={numberRows}
        rowsPerPageOptions={[5, 10, 15]}
        sortable={true}
        sortField={sortField}
        sortOrder={sortOrder}
        summary={resourcesContext.messages['dataflows']}
        totalRecords={totalRecords}
        value={dataflows}>
        {renderTableColumns(dataflows)}
      </DataTable>
    );
  };

  const renderStatusBodyColumn = rowData => <div>{capitalize(rowData.status)}</div>;

  return (
    <PublicLayout>
      <div className={`${styles.container}  rep-container`} style={contentStyles}>
        {renderPublicCountryInformationTitle()}
        {renderFilters()}
        {renderPublicCountryInformation()}
      </div>
    </PublicLayout>
  );
};
