import { Fragment, useContext } from 'react';

import isEmpty from 'lodash/isEmpty';

import { config } from 'conf';

import styles from './DataflowsList.module.scss';

import { DataflowsItem } from './_components/DataflowsItem';
import { ReferencedDataflowItem } from './_components/ReferencedDataflowItem';
import { Spinner } from 'views/_components/Spinner';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

const DataflowsList = ({
  className,
  data,
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

  const renderDataflowItem = dataflow => {
    switch (visibleTab) {
      case config.dataflowType.BUSINESS.key:
      case config.dataflowType.CITIZEN_SCIENCE.key:
      case config.dataflowType.REPORTING.key:
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
    if (isLoading) {
      return <Spinner style={{ top: 0 }} />;
    }

    if (isEmpty(data)) {
      const emptyDataflowsMessage = {
        business: 'thereAreNoBusinessDataflows',
        reference: 'thereAreNoReferenceDataflows',
        citizenScience: 'thereAreNoCitizenScienceDataflows',
        reporting: 'thereAreNoReportingDataflows'
      };

      return <div className={styles.noDataflows}>{resourcesContext.messages[emptyDataflowsMessage[visibleTab]]}</div>;
    }

    if (isEmpty(filteredData)) {
      return <div className={styles.noDataflows}>{resourcesContext.messages['noDataflowsWithSelectedParameters']}</div>;
    }

    return filteredData.map((dataflow, i) => (
      <Fragment key={dataflow.id}>
        {renderDataflowItem(dataflow)}
        {!isFilteredByPinned() && pinnedSeparatorIndex === i ? <hr className={styles.pinnedSeparator} /> : null}
      </Fragment>
    ));
  };

  return <div className={`${styles.wrap} ${className}`}>{renderContent()}</div>;
};

export { DataflowsList };
