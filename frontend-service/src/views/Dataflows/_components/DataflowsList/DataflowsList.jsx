import { Fragment, useContext } from 'react';

import isEmpty from 'lodash/isEmpty';

import { config } from 'conf';

import styles from './DataflowsList.module.scss';

import { DataflowsItem } from './_components/DataflowsItem';
import { ReferencedDataflowItem } from './_components/ReferencedDataflowItem';
import { Spinner } from 'views/_components/Spinner';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const DataflowsList = ({
  className,
  data,
  isAdmin,
  isCustodian,
  isFiltered,
  isLoading,
  pinnedSeparatorIndex,
  reorderDataflows,
  visibleTab
}) => {
  const resourcesContext = useContext(ResourcesContext);

  const isFilteredByPinned = () =>
    data.filter(dataflow => dataflow.pinned === 'pinned').length === data.length ||
    data.filter(dataflow => dataflow.pinned === 'unpinned').length === data.length;

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
      return (
        <div className={styles.spinnerDiv}>
          <Spinner className={styles.spinner} />
        </div>
      );
    }

    if (isEmpty(data)) {
      const emptyDataflowsMessage = {
        business: 'thereAreNoBusinessDataflows',
        reference: 'thereAreNoReferenceDataflows',
        citizenScience: 'thereAreNoCitizenScienceDataflows',
        reporting: 'thereAreNoReportingDataflows'
      };

      if (isFiltered) {
        return (
          <div className={styles.noDataflows}>{resourcesContext.messages['noDataflowsWithSelectedParameters']}</div>
        );
      } else {
        return <div className={styles.noDataflows}>{resourcesContext.messages[emptyDataflowsMessage[visibleTab]]}</div>;
      }
    }

    return data.map((dataflow, index) => (
      <Fragment key={dataflow.id}>
        {renderDataflowItem(dataflow)}
        {!isFilteredByPinned() && pinnedSeparatorIndex === index ? <hr className={styles.pinnedSeparator} /> : null}
      </Fragment>
    ));
  };

  return <div className={`${styles.wrap} ${className}`}>{renderContent()}</div>;
};
