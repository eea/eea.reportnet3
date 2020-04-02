import React, { useContext } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import isEmpty from 'lodash/isEmpty';

import styles from './TableView.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { Checkbox } from 'ui/views/_components/Checkbox';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const TableView = ({ checkedObligation, data, onSelectObl }) => {
  const resources = useContext(ResourcesContext);

  const onLoadCheckButton = row => (
    <div className={styles.checkColum}>
      <Checkbox
        id={`${row.id}_checkbox`}
        isChecked={checkedObligation.title === row.title}
        onChange={() => onSelectObl(row)}
        role="checkbox"
      />
    </div>
  );

  const onLoadTitleTemplate = row => (
    <div className={styles.titleColum}>
      {row.title}
      <FontAwesomeIcon
        className={styles.linkIcon}
        icon={AwesomeIcons('externalLink')}
        onMouseDown={() => window.open(`http://rod3.devel1dub.eionet.europa.eu/obligations/${row.id}`)}
      />
    </div>
  );

  const renderCheckColum = <Column key="checkId" body={row => onLoadCheckButton(row)} />;

  const renderColumns = data => {
    const repOblCols = [];
    const repOblKeys = !isEmpty(data) ? Object.keys(data[0]) : [];
    repOblCols.push(
      repOblKeys
        .filter(key => key !== 'id')
        .map(obligation => {
          let template = null;
          if (obligation === 'title') template = onLoadTitleTemplate;
          return (
            <Column
              columnResizeMode="expand"
              field={obligation}
              header={resources.messages[obligation]}
              key={obligation}
              body={template}
            />
          );
        })
    );
    return [renderCheckColum, ...repOblCols];
  };

  return (
    <DataTable
      autoLayout={true}
      // onRowClick={event => onSelectObl(event.data)}
      paginator={true}
      rows={10}
      rowsPerPageOptions={[5, 10, 15]}
      totalRecords={data.length}
      value={data}>
      {renderColumns(data)}
    </DataTable>
  );
};
