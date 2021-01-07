import React, { Fragment, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import keys from 'lodash/keys';

import styles from './NationalSystems.module.scss';

import { tables } from './nationalSystems.webform.json';

import { Button } from 'ui/views/_components/Button';
import { Spinner } from 'ui/views/_components/Spinner';
import { Toolbar } from 'ui/views/_components/Toolbar';
import { WebformTable } from 'ui/views/Webforms/_components/WebformTable';
import { NationalSystemsTable } from './_components/NationalSystemsTable';

import { DatasetService } from 'core/services/Dataset';

import { TextUtils } from 'ui/views/_functions/Utils/TextUtils';
import { WebformsUtils } from 'ui/views/Webforms/_functions/Utils/WebformsUtils';

export const NationalSystems = ({ dataflowId, datasetId, isReporting, state }) => {
  const { datasetSchema } = state;

  const nationalSystemsReducer = (state, { type, payload }) => {
    switch (type) {
      case 'INITIAL_LOAD':
        return { ...state, ...payload };

      default:
        return state;
    }
  };

  const [nationalSystemsState, nationalSystemsDispatch] = useReducer(nationalSystemsReducer, {
    data: [],
    isVisible: {}
  });

  useEffect(() => {
    // initialLoad();
  }, []);

  const initialLoad = () => {
    // const data = mergeArrays(tables, datasetSchema.tables, 'name', 'tableSchemaName');

    const result = [];
    for (let i = 0; i < tables.length; i++) {
      result.push({
        ...tables[i],
        ...datasetSchema.tables.find(
          element =>
            !isNil(element['tableSchemaName']) &&
            !isNil(tables[i]['name']) &&
            TextUtils.areEquals(element['tableSchemaName'], tables[i]['name'])
        )
      });
    }
    console.log('result', result);

    // nationalSystemsDispatch({ type: 'INITIAL_LOAD', payload: { data } });
  };

  const renderHeaders = () => {
    return (
      <Button
        className={`${styles.headerButton}`}
        icon={'table'}
        // label={webform.label}
        // onClick={() => onChangeWebformTab(webform.name)}
        // style={{ display: isReporting && !isCreated ? 'none' : '' }}
      />
    );
  };

  return (
    <div>
      <Toolbar className={styles.toolbar}>
        <div className="p-toolbar-group-left">{renderHeaders()}</div>
      </Toolbar>
      <NationalSystemsTable
        // data={nationalSystemsState.data}
        datasetId={datasetId}
        schemaTables={datasetSchema.tables[0]}
        tables={tables[0]}
        tableSchemaId={datasetSchema.tables[0].tableSchemaId}
      />
    </div>
  );
};
