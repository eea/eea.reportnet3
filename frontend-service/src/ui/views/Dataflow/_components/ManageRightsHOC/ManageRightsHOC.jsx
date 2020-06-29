import React, { useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';

import { reducer } from './_functions/Reducers/contributorReducer.js';
import { getInitialData } from './_functions/Utils/contributorUtils';

import { ManageRights } from './_components/ManageRights.jsx';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const ManageRightsHOC = ({ dataflowState, dataflowId, isActiveManageRightsDialog }) => {
  const resources = useContext(ResourcesContext);

  const initialState = {
    initialContributors: [],
    isVisibleConfirmDeleteDialog: false,
    refresher: false,
    contributorsHaveError: [],
    contributorIdToDelete: '',
    contributors: [],
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
    if (isActiveManageRightsDialog === false && !isEmpty(formState.contributorsHaveError)) {
      formDispatcher({
        type: 'REFRESH'
      });
    }
  }, [isActiveManageRightsDialog]);

  return <ManageRights formState={formState} formDispatcher={formDispatcher} dataflowId={dataflowId} />;
};

export { ManageRightsHOC };
