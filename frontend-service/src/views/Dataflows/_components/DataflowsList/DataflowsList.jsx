import { Fragment, useContext, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import { config } from 'conf';

import styles from './DataflowsList.module.scss';

import { DataflowsItem } from './_components/DataflowsItem';
import { ReferencedDataflowItem } from './_components/ReferencedDataflowItem';
import { Spinner } from 'views/_components/Spinner';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

const DataflowsList = ({
  className,
  content = {},
  filteredData,
  isAdmin,
  isCustodian,
  isLoading,
  pinnedSeparatorIndex,
  reorderDataflows,
  visibleTab
}) => {
  const resourcesContext = useContext(ResourcesContext);

  const isFilteredByPinned = () =>
    filteredData.filter(dataflow => dataflow.pinned === 'pinned').length === filteredData.length ||
    filteredData.filter(dataflow => dataflow.pinned === 'unpinned').length === filteredData.length;

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

  return <div className={`${styles.wrap} ${className}`}>{renderContent()}</div>;
};

export { DataflowsList };
