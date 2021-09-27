import { useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';
import ReactTooltip from 'react-tooltip';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';
import { getUrl } from 'repositories/_utils/UrlUtils';
import { routes } from 'conf/routes';

import styles from './PublicCountryInformation.module.scss';

import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { DownloadFile } from 'views/_components/DownloadFile';
import { Spinner } from 'views/_components/Spinner';
import { PublicLayout } from 'views/_components/Layout/PublicLayout';
import { Title } from 'views/_components/Title';

import { DataflowService } from 'services/DataflowService';
import { DatasetService } from 'services/DatasetService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { ThemeContext } from 'views/_functions/Contexts/ThemeContext';

import { useBreadCrumbs } from 'views/_functions/Hooks/useBreadCrumbs';

import { CurrentPage } from 'views/_functions/Utils';
import { DataflowUtils } from 'services/_utils/DataflowUtils';

export const PublicCountryInformation = withRouter(({ match, history }) => {
  const {
    params: { countryCode }
  } = match;

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const themeContext = useContext(ThemeContext);

  const baseRod3Url = 'https://rod.eionet.europa.eu';

  const [contentStyles, setContentStyles] = useState({});
  const [countryName, setCountryName] = useState('');
  const [dataflows, setDataflows] = useState([]);
  const [firstRow, setFirstRow] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [numberRows, setNumberRows] = useState(10);
  const [totalRecords, setTotalRecords] = useState(0);
  const [sortField, setSortField] = useState('');
  const [sortOrder, setSortOrder] = useState(0);

  useBreadCrumbs({ currentPage: CurrentPage.PUBLIC_COUNTRY, countryCode, history });

  useEffect(() => {
    onLoadPublicCountryInformation(sortOrder, sortField, firstRow, numberRows);
  }, []);

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

  const getHeader = fieldHeader => {
    switch (fieldHeader) {
      case 'isReleasable':
        return resourcesContext.messages['status'];
      case 'isReleased':
        return resourcesContext.messages['delivered'];
      case 'publicFilesNames':
        return resourcesContext.messages['files'];
      case 'referencePublicFilesNames':
        return resourcesContext.messages['referenceDatasets'];
      default:
        return resourcesContext.messages[fieldHeader];
    }
  };

  const onChangePage = event => {
    const isChangedPage = true;
    setNumberRows(event.rows);
    setFirstRow(event.first);
    onLoadPublicCountryInformation(sortOrder, sortField, event.first, event.rows, isChangedPage);
  };

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
        notificationContext.add({
          type: 'DOWNLOAD_DATASET_FILE_NOT_FOUND_EVENT'
        });
      } else {
        notificationContext.add({
          type: 'DOWNLOAD_DATASET_FILE_ERROR'
        });
      }
    }
  };

  const onLoadPublicCountryInformation = async (sortOrder, sortField, firstRow, numberRows, isChangedPage) => {
    setIsLoading(true);
    try {
      if (sortOrder === -1) {
        sortOrder = 0;
      }
      let pageNum = isChangedPage ? Math.floor(firstRow / numberRows) : 0;
      const data = await DataflowService.getPublicDataflowsByCountryCode(
        countryCode,
        sortOrder,
        pageNum,
        numberRows,
        sortField
      );
      setTotalRecords(data.totalRecords);
      setPublicInformation(data.publicDataflows);
    } catch (error) {
      console.error('PublicCountryInformation - onLoadPublicCountryInformation.', error);
      notificationContext.add({ type: 'LOAD_DATAFLOWS_BY_COUNTRY_ERROR' });
    } finally {
      setIsLoading(false);
    }
  };

  const onSort = event => {
    setSortOrder(event.sortOrder);
    setSortField(event.sortField);
    onLoadPublicCountryInformation(event.sortOrder, event.sortField, firstRow, numberRows);
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
          isReleasable: dataflow.isReleasable,
          isReleased: dataset.isReleased,
          legalInstrument: dataflow.obligation?.legalInstrument,
          name: dataflow.name,
          obligation: dataflow.obligation,
          publicFilesNames: publicFileNames,
          referencePublicFilesNames: referencePublicFileNames,
          deliveryDate: dataset.releaseDate,
          deliveryStatus: !dataset.isReleased
            ? config.datasetStatus.PENDING.label
            : !dataflow.manualAcceptance
            ? config.datasetStatus.DELIVERED.label
            : DataflowUtils.getTechnicalAcceptanceStatus(dataflow.datasets.map(dataset => dataset.status)),
          restrictFromPublic: dataflow.datasets ? dataflow.datasets[0].restrictFromPublic : false
        };
      });
    setDataflows(publicDataflows);
  };

  const getOrderedColumns = dataflows => {
    const dataflowsWithPriority = [
      { id: 'id', index: 0 },
      { id: 'name', index: 1 },
      { id: 'obligation', index: 2 },
      { id: 'legalInstrument', index: 3 },
      { id: 'deadline', index: 4 },
      { id: 'isReleasable', index: 5 },
      { id: 'deliveryDate', index: 6 },
      { id: 'deliveryStatus', index: 7 },
      { id: 'referencePublicFilesNames', index: 8 },
      { id: 'publicFilesNames', index: 9 }
    ];

    return dataflows
      .map(field => dataflowsWithPriority.filter(e => field === e.id))
      .flat()
      .sort((a, b) => a.index - b.index)
      .map(orderedField => orderedField.id);
  };

  const renderColumns = dataflows => {
    const fieldColumns = getOrderedColumns(Object.keys(dataflows[0]))
      .filter(key => !key.includes('id'))
      .map(field => {
        let template = null;
        if (field === 'isReleasable') template = renderIsReleasableBodyColumn;
        if (field === 'isReleased') template = renderIsReleasedBodyColumn;
        if (field === 'legalInstrument') template = renderLegalInstrumentBodyColumn;
        if (field === 'name') template = renderDataflowNameBodyColumn;
        if (field === 'obligation') template = renderObligationBodyColumn;
        if (field === 'publicFilesNames') template = renderDownloadFileBodyColumn;
        if (field === 'referencePublicFilesNames') template = renderDownloadReferenceFileBodyColumn;
        return (
          <Column
            body={template}
            field={field}
            header={getHeader(field)}
            key={field}
            sortable={field === 'publicFilesNames' || field === 'referencePublicFilesNames' ? false : true}
          />
        );
      });

    return fieldColumns;
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

  const renderIsReleasableBodyColumn = rowData => (
    <div>{rowData.isReleasable ? resourcesContext.messages['open'] : resourcesContext.messages['closed']}</div>
  );

  const renderIsReleasedBodyColumn = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.isReleased && <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} />}
    </div>
  );

  const renderLegalInstrumentBodyColumn = rowData => (
    <div onClick={e => e.stopPropagation()}>
      {rowData.legalInstrument?.id
        ? renderRedirectText(
            rowData.legalInstrument?.alias,
            `${baseRod3Url}/instruments/${rowData.legalInstrument?.id}`
          )
        : rowData.legalInstrument?.alias}
    </div>
  );

  const renderDataflowNameBodyColumn = rowData => (
    <div onClick={e => e.stopPropagation()}>
      <span className={styles.cellWrapper}>
        {rowData.name}{' '}
        <FontAwesomeIcon
          aria-hidden={false}
          className={`p-breadcrumb-home ${styles.link}`}
          data-for="navigateTooltip"
          data-tip
          icon={AwesomeIcons('externalUrl')}
          onClick={e => {
            e.preventDefault();
            history.push(getUrl(routes.PUBLIC_DATAFLOW_INFORMATION, { dataflowId: rowData.id }, true));
          }}
        />
        <ReactTooltip border={true} className={styles.tooltipClass} effect="solid" id="navigateTooltip" place="top">
          <span>{resourcesContext.messages['navigateToDataflow']}</span>
        </ReactTooltip>
      </span>
    </div>
  );

  const renderObligationBodyColumn = rowData => (
    <div onClick={e => e.stopPropagation()}>
      {rowData.obligation?.obligationId
        ? renderRedirectText(
            rowData.obligation?.title,
            `${baseRod3Url}/obligations/${rowData.obligation?.obligationId}`
          )
        : rowData.obligation?.title}
    </div>
  );

  const renderRedirectText = (text, url) => (
    <span>
      {text}{' '}
      <a href={url} rel="noreferrer" target="_blank" title={text}>
        <FontAwesomeIcon aria-hidden={false} className="p-breadcrumb-home" icon={AwesomeIcons('externalUrl')} />
      </a>
    </span>
  );

  return (
    <PublicLayout>
      <div className={`${styles.container}  rep-container`} style={contentStyles}>
        {!isEmpty(countryName) && (
          <Title
            icon={'clone'}
            iconSize={'4rem'}
            subtitle={resourcesContext.messages['dataflows']}
            title={countryName}
          />
        )}
        {isLoading ? (
          <Spinner className={styles.isLoading} />
        ) : isEmpty(countryName) ? (
          <div className={styles.noDataflows}>{resourcesContext.messages['wrongUrlCountryCode']}</div>
        ) : isEmpty(dataflows) ? (
          <div className={styles.noDataflows}>{resourcesContext.messages['noDataflows']}</div>
        ) : (
          <div className={styles.countriesList}>
            <DataTable
              autoLayout={true}
              first={firstRow}
              lazy={true}
              onPage={onChangePage}
              onSort={onSort}
              paginator={true}
              paginatorRight={
                <span>{`${resourcesContext.messages['totalRecords']} ${totalRecords} ${resourcesContext.messages[
                  'records'
                ].toLowerCase()}`}</span>
              }
              rows={numberRows}
              rowsPerPageOptions={[5, 10, 15]}
              sortField={sortField}
              sortOrder={sortOrder}
              sortable={true}
              totalRecords={totalRecords}
              value={dataflows}>
              {renderColumns(dataflows)}
            </DataTable>
          </div>
        )}
      </div>
    </PublicLayout>
  );
});
