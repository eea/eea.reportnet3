import React, { useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';
import ReactTooltip from 'react-tooltip';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';

import styles from './PublicCountryInformation.module.scss';

import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { Spinner } from 'ui/views/_components/Spinner';
import { PublicLayout } from 'ui/views/_components/Layout/PublicLayout';
import { Title } from 'ui/views/_components/Title';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';

import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';

import { CurrentPage } from 'ui/views/_functions/Utils';

export const PublicCountryInformation = withRouter(({ match, history }) => {
  const {
    params: { countryCode }
  } = match;

  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const themeContext = useContext(ThemeContext);

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
    let header;
    switch (fieldHeader) {
      case 'name':
        header = resources.messages['name'];
        break;
      case 'obligation':
        header = resources.messages['obligationTitle'];
        break;
      case 'legalInstrument':
        header = resources.messages['legalInstrument'];
        break;
      case 'deadline':
        header = resources.messages['deadline'];
        break;
      case 'isReleasable':
        header = resources.messages['status'];
        break;
      case 'isReleased':
        header = resources.messages['delivered'];
        break;
      case 'releaseDate':
        header = resources.messages['releasedDate'];
        break;
      case 'publicFilesNames':
        header = resources.messages['files'];
        break;
      default:
        header = fieldHeader;
        break;
    }
    return header;
  };

  const getPublicFileName = fileName => {
    const splittedFileName = fileName.split('-');
    return splittedFileName[1];
  };

  const onChangePage = event => {
    const isChangedPage = true;
    setNumberRows(event.rows);
    setFirstRow(event.first);
    onLoadPublicCountryInformation(sortOrder, sortField, event.first, event.rows, isChangedPage);
  };

  const onFileDownload = async (dataflowId, dataProviderId, fileName) => {
    try {
      const fileContent = await DatasetService.downloadDatasetFileData(dataflowId, dataProviderId, fileName);

      DownloadFile(fileContent.data, fileName);
    } catch (error) {
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
      const { data } = await DataflowService.getPublicDataflowsByCountryCode(
        countryCode,
        sortOrder,
        pageNum,
        numberRows,
        sortField
      );
      setTotalRecords(data.totalRecords);
      parseDataflows(data.publicDataflows);
    } catch (error) {
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

  const parseDataflows = dataflows => {
    const parsedDataflows = [];
    dataflows.forEach(dataflow => {
      const isReleased = dataflow.datasets.some(dataset => dataset.isReleased);
      const publicFilesNames = [];
      dataflow.datasets.forEach(dataset => {
        if (!isNil(dataset.publicFileName)) {
          publicFilesNames.push({ dataProviderId: dataset.dataProviderId, fileName: dataset.publicFileName });
        }
      });
      const parsedDataflow = {
        id: dataflow.id,
        name: dataflow.name,
        obligation: dataflow.obligation,
        legalInstrument: dataflow.obligation.legalInstruments,
        deadline: dataflow.expirationDate,
        isReleased: isReleased,
        isReleasable: dataflow.isReleasable,
        releaseDate: isReleased ? dataflow.datasets[0].releaseDate : '-',
        restrictFromPublic: dataflow.datasets[0].restrictFromPublic,
        publicFilesNames: publicFilesNames
      };
      parsedDataflows.push(parsedDataflow);
    });
    setDataflows(parsedDataflows);
  };

  const getOrderedColumns = dataflows => {
    const dataflowsWithPriority = [
      { id: 'id', index: 0 },
      { id: 'name', index: 1 },
      { id: 'obligation', index: 2 },
      { id: 'legalInstrument', index: 3 },
      { id: 'deadline', index: 4 },
      { id: 'isReleasable', index: 5 },
      { id: 'isReleased', index: 6 },
      { id: 'releaseDate', index: 7 },
      { id: 'publicFilesNames', index: 8 }
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
        if (field === 'obligation') template = renderObligationBodyColumn;
        if (field === 'publicFilesNames') template = renderDownloadFileBodyColumn;
        return (
          <Column
            body={template}
            field={field}
            header={getHeader(field)}
            key={field}
            sortable={field === 'publicFilesNames' ? false : true}
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
              onClick={() => onFileDownload(rowData.id, publicFileName.dataProviderId, publicFileName.fileName)}>
              <FontAwesomeIcon
                className={styles.cursorPointer}
                icon={AwesomeIcons('7z')}
                data-tip
                data-for={publicFileName.fileName}
              />
              <ReactTooltip className={styles.tooltipClass} effect="solid" id={publicFileName.fileName} place="top">
                <span>{getPublicFileName(publicFileName.fileName)}</span>
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
            icon={AwesomeIcons('lock')}
            data-tip
            data-for={'restrictFromPublicField'}
          />
          <ReactTooltip className={styles.tooltipClass} effect="solid" id={'restrictFromPublicField'} place="top">
            <span>{resources.messages['restrictFromPublicField']}</span>
          </ReactTooltip>
        </div>
      );
    }
  };

  const renderIsReleasableBodyColumn = rowData => (
    <div>{rowData.isReleasable ? resources.messages['open'] : resources.messages['closed']}</div>
  );

  const renderIsReleasedBodyColumn = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.isReleased && <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} />}
    </div>
  );

  const renderLegalInstrumentBodyColumn = rowData => (
    <div onClick={e => e.stopPropagation()}>
      {renderRedirectText(
        rowData.legalInstrument.alias,
        `https://rod.eionet.europa.eu/instruments/${rowData.legalInstrument.id}`
      )}
    </div>
  );

  const renderObligationBodyColumn = rowData => (
    <div onClick={e => e.stopPropagation()}>
      {renderRedirectText(
        rowData.obligation.title,
        `https://rod.eionet.europa.eu/obligations/${rowData.obligation.obligationId}`
      )}
    </div>
  );

  const renderRedirectText = (text, url) => (
    <span>
      {text}{' '}
      <a href={url} target="_blank" title={text}>
        <FontAwesomeIcon aria-hidden={false} className="p-breadcrumb-home" icon={AwesomeIcons('externalLink')} />
      </a>
    </span>
  );

  return (
    <PublicLayout>
      <div className={`${styles.container} ${isLoading ? styles.isLoading : ''} rep-container`} style={contentStyles}>
        {!isLoading ? (
          !isEmpty(dataflows) ? (
            <>
              <Title icon={'clone'} iconSize={'4rem'} subtitle={resources.messages['dataflows']} title={countryName} />
              <div className={styles.countriesList}>
                <DataTable
                  autoLayout={true}
                  first={firstRow}
                  lazy={true}
                  onPage={onChangePage}
                  onSort={onSort}
                  paginator={true}
                  paginatorRight={
                    <span>{`${resources.messages['totalRecords']} ${totalRecords} ${resources.messages[
                      'records'
                    ].toLowerCase()}`}</span>
                  }
                  rows={numberRows}
                  rowsPerPageOptions={[5, 10, 15]}
                  sortable={true}
                  sortField={sortField}
                  sortOrder={sortOrder}
                  totalRecords={totalRecords}
                  value={dataflows}>
                  {renderColumns(dataflows)}
                </DataTable>
                <div className={styles.tableLegendContainer}>
                  <span>*</span>
                  <FontAwesomeIcon className={styles.tableLegendIcon} icon={AwesomeIcons('lock')} />
                  <div className={styles.tableLegendText}> {resources.messages['restrictFromPublicField']}</div>
                </div>
              </div>
            </>
          ) : (
            <div className={styles.noDataflows}>{resources.messages['noDataflows']}</div>
          )
        ) : (
          <Spinner style={{ top: 0, left: 0 }} />
        )}
      </div>
    </PublicLayout>
  );
});
