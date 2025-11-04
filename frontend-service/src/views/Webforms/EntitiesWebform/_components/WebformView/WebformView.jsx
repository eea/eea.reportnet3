import { useEffect, useReducer, useContext } from 'react';

import isNil from 'lodash/isNil';
import keys from 'lodash/keys';
import pickBy from 'lodash/pickBy';
import uniqueId from 'lodash/uniqueId';

import styles from './WebformView.module.scss';

import { Button } from 'views/_components/Button';
import { Spinner } from 'views/_components/Spinner';
import { Toolbar } from 'views/_components/Toolbar';
import { WebformTable } from 'views/Webforms/_components/WebformTable';

import { webformViewReducer } from './_functions/Reducers/webformViewReducer';

import { WebformsUtils } from 'views/Webforms/_functions/Utils/WebformsUtils';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const WebformView = ({
  bigData,
  onFieldUpdate,
  data,
  dataProviderId,
  dataflowId,
  datasetId,
  datasetSchema,
  datasetSchemaId,
  getFieldSchemaId,
  isAddingRootTableId = false,
  isIcebergCreated,
  isRefresh,
  isReporting,
  updatingField,
  isViewMode,
  rootPkFieldId,
  rootTableName,
  selectedTable,
  setTableSchemaId,
  state,
  tables
}) => {
  const tableSchemaNames = state.schemaTables.map(table => table.name);
  const { getWebformTabs } = WebformsUtils;
  const resourcesContext = useContext(ResourcesContext);

  const [webformViewState, webformViewDispatch] = useReducer(
    webformViewReducer,
    {
      isLoading: false
    },
    initialState => ({
      ...initialState,
      isVisible: getWebformTabs(
        tables.map(table => table.label),
        state.schemaTables,
        tables
      )
    })
  );

  const { isLoading, isVisible } = webformViewState;

  useEffect(() => {
    const visibleTable = Object.keys(isVisible).filter(key => isVisible[key])[0];
    const visibleTableId = data.filter(table => table.label === visibleTable)[0].tableSchemaId;

    setTableSchemaId(visibleTableId);
  }, [isVisible]);

  const setIsLoading = value => webformViewDispatch({ type: 'SET_IS_LOADING', payload: { value } });

  const onChangeWebformTab = name => {
    const newIsVisible = Object.fromEntries(Object.keys(isVisible).map(tab => [tab, tab === name]));

    webformViewDispatch({
      type: 'ON_CHANGE_TAB',
      payload: { isVisible: newIsVisible }
    });
  };

  const renderWebFormHeaders = () => {
    const filteredTabs = data.filter(header => tableSchemaNames.includes(header.name));
    const headers = filteredTabs.map(tab => tab.header || tab.name);
    return data
      .filter(table => table.isVisible)
      .map(webform => {
        const isCreated = headers.includes(webform.name);
        return (
          <Button
            className={`${styles.headerButton} ${isVisible[webform.label] ? 'p-button-primary' : 'p-button-secondary'}`}
            disabled={isLoading}
            icon={!isCreated ? 'info' : 'table'}
            iconClasses={!isVisible[webform.title] ? 'info' : ''}
            iconPos={!isCreated ? 'right' : 'left'}
            key={uniqueId()}
            label={webform.label}
            onClick={() => onChangeWebformTab(webform.label)}
            style={{ display: isReporting && !isCreated ? 'none' : '' }}
          />
        );
      });
  };

  const renderWebFormContent = () => {
    const visibleTitle = keys(pickBy(isVisible))[0];
    const visibleContent = data.filter(table => table.label === visibleTitle && table.isVisible)[0];

    return (
      <WebformTable
        bigData={bigData}
        dataflowId={dataflowId}
        dataProviderId={dataProviderId}
        datasetId={datasetId}
        datasetSchema={datasetSchema}
        datasetSchemaId={datasetSchemaId}
        getFieldSchemaId={getFieldSchemaId}
        isIcebergCreated={isIcebergCreated}
        isRefresh={isRefresh}
        isReporting={isReporting}
        isViewMode={isViewMode}
        onFieldUpdate={onFieldUpdate}
        onTabChange={isVisible}
        rootPkFieldId={rootPkFieldId}
        rootTableName={rootTableName}
        selectedTable={selectedTable}
        setIsLoading={setIsLoading}
        updatingField={updatingField}
        webform={visibleContent}
        webformType={'ENTITIES'}
      />
    );
  };

  if (isAddingRootTableId) {
    return <Spinner style={{ top: 0, marginBottom: '2rem' }} />;
  }

  const renderViewModeMessage = () => {
    if ((isViewMode && isIcebergCreated) || (isViewMode && !bigData)) {
      return (
        <div className={styles.viewModeWarning} role="alert">
          <i className={`pi pi-info-circle ${styles.infoIcon}`} />
          {resourcesContext.messages['viewModeMessage']}
        </div>
      );
    } else if (isViewMode && !isIcebergCreated) {
      return (
        <div className={styles.viewModeWarning} role="alert">
          <i className={`pi pi-info-circle ${styles.infoIcon}`} />
          {resourcesContext.messages['viewModeMessageIceberg']}
        </div>
      );
    }
    return null;
  };

  return (
    <div className={styles.webform}>
      <Toolbar className={styles.toolbar}>
        <div className="p-toolbar-group-left">{renderWebFormHeaders()}</div>
      </Toolbar>
      {renderViewModeMessage()}
      {renderWebFormContent()}
    </div>
  );
};
