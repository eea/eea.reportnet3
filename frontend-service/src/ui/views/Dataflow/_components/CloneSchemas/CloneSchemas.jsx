import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import { CardsView } from 'ui/views/_components/CardsView';

import { DataflowService } from 'core/services/Dataflow';

import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { cloneSchemasReducer } from './_functions/Reducers/cloneSchemasReducer';

import { CloneSchemasUtils } from './_functions/Utils/CloneSchemasUtils';

export const CloneSchemas = dataflowId => {
  const user = useContext(UserContext);

  const [cloneSchemasState, cloneSchemasDispatch] = useReducer(cloneSchemasReducer, {
    accepted: [],
    allDataflows: {},
    completed: [],
    isLoading: true,
    pending: []
  });

  useEffect(() => {
    onLoadDataflows();
  }, []);

  const isLoading = value => cloneSchemasDispatch({ type: 'IS_LOADING', payload: { value } });

  const onLoadDataflows = async () => {
    isLoading(true);
    try {
      const allDataflows = await DataflowService.all(user.contextRoles);
      cloneSchemasDispatch({
        type: 'INITIAL_LOAD',
        payload: {
          accepted: CloneSchemasUtils.parseDataflowsList(allDataflows.accepted),
          allDataflows,
          completed: allDataflows.completed,
          pending: allDataflows.pending
        }
      });
    } catch (error) {
      console.error('onLoadDataflows error: ', error);
    } finally {
      isLoading(false);
    }
  };

  const renderCardView = () => (
    <CardsView
      // checkedCard={reportingObligationState.oblChoosed}
      data={cloneSchemasState.accepted}
      // handleRedirect={onOpenObligation}
      // onChangePagination={onChangePagination}
      // onSelectCard={onSelectObl}
      // pagination={reportingObligationState.pagination}
    />
  );

  return (
    <Fragment>
      <div>Dataflows</div>
      {renderCardView()}
    </Fragment>
  );
};
