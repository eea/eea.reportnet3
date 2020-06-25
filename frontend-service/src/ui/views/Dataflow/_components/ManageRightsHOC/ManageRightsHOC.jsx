import React, { useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';

import { reducer } from './_functions/Reducers/representativeReducer.js';
import { getInitialData } from './_functions/Utils/rightsUtils';

import { ManageRights } from './_components/ManageRights.jsx';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const ManageRightsHOC = ({ dataflowState, dataflowId, isActiveManageRightsDialog }) => {
  const resources = useContext(ResourcesContext);

  const initialState = {
    initialRepresentatives: [],
    isVisibleConfirmDeleteDialog: false,
    refresher: false,
    representativeHasError: [],
    representativeIdToDelete: '',
    representatives: [],
    accountInputHeader: dataflowState.isCustodian
      ? resources.messages['editorsAccountColumn']
      : resources.messages['reportersAccountColumn'],
    deleteConfirmHeader: dataflowState.isCustodian
      ? resources.messages['editorsRightsDialogConfirmDeleteHeader']
      : resources.messages['reportersRightsDialogConfirmDeleteHeader'],
    deleteConfirmMessage: dataflowState.isCustodian
      ? resources.messages['editorsRightsDialogConfirmDeleteQuestion']
      : resources.messages['reportersRightsDialogConfirmDeleteQuestion']
  };

  const [formState, formDispatcher] = useReducer(reducer, initialState);

  useEffect(() => {
    getInitialData(formDispatcher, dataflowId, formState);
  }, [formState.refresher]);

  useEffect(() => {
    if (isActiveManageRightsDialog === false && !isEmpty(formState.representativeHasError)) {
      formDispatcher({
        type: 'REFRESH'
      });
    }
  }, [isActiveManageRightsDialog]);

  return <ManageRights formState={formState} formDispatcher={formDispatcher} dataflowId={dataflowId} />;
};

export { ManageRightsHOC };
