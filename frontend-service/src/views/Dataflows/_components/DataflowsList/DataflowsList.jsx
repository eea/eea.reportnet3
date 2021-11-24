import { Fragment, useContext, useEffect, useLayoutEffect, useState } from 'react';

import intersection from 'lodash/intersection';
import isEmpty from 'lodash/isEmpty';
import orderBy from 'lodash/orderBy';
import pull from 'lodash/pull';

import { config } from 'conf';

import styles from './DataflowsList.module.scss';

import { DataflowsItem } from './_components/DataflowsItem';
import { Filters } from 'views/_components/Filters';
import { MyFilters } from 'views/_components/Filters/MyFilters';
import { ReferencedDataflowItem } from './_components/ReferencedDataflowItem';
import { Spinner } from 'views/_components/Spinner';

import { UserService } from 'services/UserService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

const DataflowsList = ({ className, content = {}, isAdmin, isCustodian, isLoading, visibleTab }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [dataToFilter, setDataToFilter] = useState({
    reporting: content['reporting'],
    business: content['business'],
    citizenScience: content['citizenScience'],
    reference: content['reference']
  });
  const [filteredData, setFilteredData] = useState(dataToFilter[visibleTab]);
  const [pinnedSeparatorIndex, setPinnedSeparatorIndex] = useState(-1);

  useLayoutEffect(() => {
    const parsedDataflows = orderBy(
      parseDataToFilter(content[visibleTab], userContext.userProps.pinnedDataflows),
      ['pinned', 'expirationDate', 'status', 'id', 'creationDate'],
      ['asc', 'asc', 'asc', 'asc', 'asc']
    );
    setDataToFilter({ ...dataToFilter, [visibleTab]: parsedDataflows });
    const orderedPinned = parsedDataflows.map(el => el.pinned === 'pinned');

    setPinnedSeparatorIndex(orderedPinned.lastIndexOf(true));
  }, [content]);

  useEffect(() => {
    const parsedDataflows = orderBy(
      filteredData,
      ['pinned', 'expirationDate', 'status', 'id', 'creationDate'],
      ['asc', 'asc', 'asc', 'asc', 'asc']
    );
    const orderedPinned = parsedDataflows.map(el => el.pinned === 'pinned');
    setPinnedSeparatorIndex(orderedPinned.lastIndexOf(true));
  }, [filteredData]);

  const parseDataToFilter = (data, pinnedDataflows) => {
    return data?.map(dataflow => ({
      id: dataflow.id,
      creationDate: dataflow.creationDate,
      description: dataflow.description,
      expirationDate: dataflow.expirationDate,
      legalInstrument: dataflow.obligation?.legalInstrument?.alias,
      name: dataflow.name,
      obligationTitle: dataflow.obligation?.title,
      obligationId: dataflow.obligation?.obligationId?.toString(),
      pinned: pinnedDataflows.some(pinnedDataflow => pinnedDataflow === dataflow.id.toString()) ? 'pinned' : 'unpinned',
      reportingDatasetsStatus: dataflow.reportingDatasetsStatus,
      showPublicInfo: dataflow.showPublicInfo,
      status: dataflow.status,
      statusKey: dataflow.statusKey,
      userRole: dataflow.userRole
    }));
  };

  const onLoadFilteredData = data => setFilteredData(data);

  const changeUserProperties = async userProperties => {
    try {
      return await UserService.updateConfiguration(userProperties);
    } catch (error) {
      console.error('DataflowsList - changeUserProperties.', error);
      notificationContext.add({ type: 'UPDATE_ATTRIBUTES_USER_SERVICE_ERROR' }, true);
    }
  };

  const isFilteredByPinned = () =>
    filteredData.filter(dataflow => dataflow.pinned === 'pinned').length === filteredData.length ||
    filteredData.filter(dataflow => dataflow.pinned === 'unpinned').length === filteredData.length;

  const reorderDataflows = async (pinnedItem, isPinned) => {
    const inmUserProperties = { ...userContext.userProps };
    const inmPinnedDataflows = intersection(
      inmUserProperties.pinnedDataflows,
      [
        ...dataToFilter.reporting,
        ...dataToFilter.reference,
        ...dataToFilter.business,
        ...dataToFilter.citizenScience
      ].map(data => data.id.toString())
    );
    if (!isEmpty(inmPinnedDataflows) && inmPinnedDataflows.includes(pinnedItem.id.toString())) {
      pull(inmPinnedDataflows, pinnedItem.id.toString());
    } else {
      inmPinnedDataflows.push(pinnedItem.id.toString());
    }
    inmUserProperties.pinnedDataflows = inmPinnedDataflows;
    await changeUserProperties(inmUserProperties);
    userContext.onChangePinnedDataflows(inmPinnedDataflows);

    const inmfilteredData = [...filteredData];
    const changedFilteredData = inmfilteredData.map(item => {
      if (item.id === pinnedItem.id) {
        item.pinned = isPinned ? 'pinned' : 'unpinned';
      }
      return item;
    });

    if (isPinned) {
      notificationContext.add(
        { type: 'DATAFLOW_PINNED_INIT', content: { customContent: { dataflowName: pinnedItem.name } } },
        true
      );
    } else {
      notificationContext.add(
        { type: 'DATAFLOW_UNPINNED_INIT', content: { customContent: { dataflowName: pinnedItem.name } } },
        true
      );
    }

    const orderedFilteredData = orderBy(
      changedFilteredData,
      ['pinned', 'expirationDate', 'status', 'id', 'creationDate'],
      ['asc', 'asc', 'asc', 'asc', 'asc']
    );

    const orderedPinned = orderedFilteredData.map(el => el.pinned);
    setPinnedSeparatorIndex(orderedPinned.lastIndexOf(true));

    const inmDataToFilter = { ...dataToFilter };
    const changedInitialData = inmDataToFilter[visibleTab].map(item => {
      if (item.id === pinnedItem.id) {
        item.pinned = isPinned ? 'pinned' : 'unpinned';
      }
      return item;
    });

    setDataToFilter({
      ...dataToFilter,
      [visibleTab]: orderBy(
        changedInitialData,
        ['pinned', 'expirationDate', 'status', 'id', 'creationDate'],
        ['asc', 'asc', 'asc', 'asc', 'asc']
      )
    });
  };

  const FILTER_OPTIONS = [
    { category: 'LEVEL_ERROR', key: 'obligation', label: 'Obligation', order: 0, type: 'INPUT' },
    { category: undefined, key: 'operationName', label: 'Another label', order: 1, type: 'INPUT', options: undefined },
    { category: 'BOOLEAN', key: 'anotherKeyName', label: 'Bool type', options: [], order: 2, type: 'MULTI_SELECT' },
    {
      type: 'INPUT',
      category: 'ANOTHER_TYPE_OF_CATEGORY',
      options: [
        { key: 'obligation', label: 'Obligation', order: 0 },
        { key: 'operationName', label: 'Another label', order: 1 }
      ]
    }
  ];

  const filterOptions = {
    reporting: [
      {
        type: 'input',
        properties: [
          { name: 'name' },
          { name: 'description' },
          { name: 'legalInstrument' },
          { name: 'obligationTitle', label: resourcesContext.messages['obligation'] },
          { name: 'obligationId' }
        ]
      },
      { type: 'multiselect', properties: [{ name: 'status' }, { name: 'userRole' }, { name: 'pinned' }] },
      {
        type: 'date',
        properties: [{ name: 'expirationDate', label: resourcesContext.messages['expirationDateFilterLabel'] }]
      },
      (isCustodian || isAdmin) && {
        type: 'date',
        properties: [{ name: 'creationDate', label: resourcesContext.messages['creationDateFilterLabel'] }]
      }
    ],
    citizenScience: [
      {
        type: 'input',
        properties: [
          { name: 'name' },
          { name: 'description' },
          { name: 'legalInstrument' },
          { name: 'obligationTitle', label: resourcesContext.messages['obligation'] },
          { name: 'obligationId' }
        ]
      },
      { type: 'multiselect', properties: [{ name: 'status' }, { name: 'userRole' }, { name: 'pinned' }] },
      {
        type: 'date',
        properties: [{ name: 'expirationDate', label: resourcesContext.messages['expirationDateFilterLabel'] }]
      },
      (isCustodian || isAdmin) && {
        type: 'date',
        properties: [{ name: 'creationDate', label: resourcesContext.messages['creationDateFilterLabel'] }]
      }
    ],
    business: [
      {
        type: 'input',
        properties: [
          { name: 'name' },
          { name: 'description' },
          { name: 'legalInstrument' },
          { name: 'obligationTitle', label: resourcesContext.messages['obligation'] },
          { name: 'obligationId' }
        ]
      },
      {
        type: 'multiselect',
        properties: [{ name: 'status' }, { name: 'userRole' }, { name: 'pinned' }]
      },
      {
        type: 'date',
        properties: [{ name: 'expirationDate', label: resourcesContext.messages['expirationDateFilterLabel'] }]
      },
      (isCustodian || isAdmin) && {
        type: 'date',
        properties: [{ name: 'creationDate', label: resourcesContext.messages['creationDateFilterLabel'] }]
      }
    ],
    reference: [
      { type: 'input', properties: [{ name: 'name' }, { name: 'description' }] },
      { type: 'multiselect', properties: [{ name: 'status' }, { name: 'pinned' }] }
    ]
  };

  const renderDataflowItem = dataflow => {
    switch (visibleTab) {
      case config.dataflowType.REPORTING.key:
        return (
          <DataflowsItem
            isAdmin={isAdmin}
            isCustodian={isCustodian}
            itemContent={dataflow}
            reorderDataflows={reorderDataflows}
          />
        );
      case config.dataflowType.BUSINESS.key:
        return (
          <DataflowsItem
            isAdmin={isAdmin}
            isCustodian={isCustodian}
            itemContent={dataflow}
            reorderDataflows={reorderDataflows}
          />
        );
      case config.dataflowType.CITIZEN_SCIENCE.key:
        return (
          <DataflowsItem
            isAdmin={isAdmin}
            isCustodian={isCustodian}
            itemContent={dataflow}
            reorderDataflows={reorderDataflows}
          />
        );
      case config.dataflowType.REFERENCE.key:
        return <ReferencedDataflowItem dataflow={dataflow} reorderDataflows={reorderDataflows} />;
      default:
        break;
    }
  };

  const renderContent = () => {
    if (isLoading) return <Spinner style={{ top: 0 }} />;

    if (isEmpty(content[visibleTab])) {
      const emptyDataflowsMessage = {
        business: 'thereAreNoBusinessDataflows',
        reference: 'thereAreNoReferenceDataflows',
        citizenScience: 'thereAreNoCitizenScienceDataflows',
        reporting: 'thereAreNoReportingDataflows'
      };

      return <div className={styles.noDataflows}>{resourcesContext.messages[emptyDataflowsMessage[visibleTab]]}</div>;
    }

    return !isEmpty(filteredData) ? (
      filteredData.map((dataflow, i) => (
        <Fragment key={dataflow.id}>
          {renderDataflowItem(dataflow)}
          {!isFilteredByPinned() && pinnedSeparatorIndex === i ? <hr className={styles.pinnedSeparator} /> : null}
        </Fragment>
      ))
    ) : (
      <div className={styles.noDataflows}>{resourcesContext.messages['noDataflowsWithSelectedParameters']}</div>
    );
  };

  return (
    <div className={`${styles.wrap} ${className}`}>
      <div className="dataflowList-filters-help-step">
        {visibleTab === 'reporting' && (
          <MyFilters
            className={'dataflowsListFilters'}
            data={dataToFilter['reporting']}
            getFilteredData={onLoadFilteredData}
            options={filterOptions['reporting'].filter(Boolean)}
            sortCategory={'pinned'}
            sortable={true}
          />
        )}

        {visibleTab === 'business' && (
          <Filters
            className={'dataflowsListFilters'}
            data={dataToFilter['business']}
            getFilteredData={onLoadFilteredData}
            options={filterOptions['business'].filter(Boolean)}
            sortCategory={'pinned'}
            sortable={true}
          />
        )}

        {visibleTab === 'citizenScience' && (
          <Filters
            className={'dataflowsListFilters'}
            data={dataToFilter['citizenScience']}
            getFilteredData={onLoadFilteredData}
            options={filterOptions['citizenScience'].filter(Boolean)}
            sortCategory={'pinned'}
            sortable={true}
          />
        )}

        {visibleTab === 'reference' && (
          <Filters
            className={'referenceDataflowsListFilters'}
            data={dataToFilter['reference']}
            getFilteredData={onLoadFilteredData}
            options={filterOptions['reference']}
            sortCategory={'pinned'}
            sortable={true}
          />
        )}
      </div>

      {renderContent()}
    </div>
  );
};

export { DataflowsList };
