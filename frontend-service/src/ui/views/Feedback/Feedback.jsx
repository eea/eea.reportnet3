import React, { Fragment, useContext, useEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { MainLayout } from 'ui/views/_components/Layout';
import { Spinner } from 'ui/views/_components/Spinner';
import { Title } from 'ui/views/_components/Title';

import { FeedbackService } from 'core/services/Feedback';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';

import { feedbackReducer } from './_functions/Reducers/feedbackReducer';

import { CurrentPage } from 'ui/views/_functions/Utils';
import { DataflowUtils } from 'ui/views/_functions/Utils/DataflowUtils';

export const Feedback = withRouter(({ match, history }) => {
  const {
    params: { dataflowId }
  } = match;

  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [feedbackState, dispatchFeedback] = useReducer(feedbackReducer, {
    dataflowName: '',
    isDialogVisible: false,
    isLoading: false,
    messages: [],
    messageToShow: ''
  });

  const { dataflowName, isDialogVisible, isLoading, messages, messageToShow } = feedbackState;

  useEffect(() => {
    onGetDataflowName();
    onGetUnreadMessages();
  }, []);

  useBreadCrumbs({ currentPage: CurrentPage.DATAFLOW_FEEDBACK, dataflowId, history });

  const loadingText = () => {
    return <span className="loading-text"></span>;
  };

  const onCloseDialog = () => {
    console.log('CLOSE');
    dispatchFeedback({ type: 'SET_IS_VISIBLE_DIALOG', payload: false });
  };

  const onGetDataflowName = async () => {
    try {
      const name = await DataflowUtils.getDataflowName(dataflowId);
      dispatchFeedback({ type: 'SET_DATAFLOW_NAME', payload: name });
    } catch (error) {
      notificationContext.add({
        type: 'DATAFLOW_DETAILS_ERROR',
        content: {}
      });
    }
  };

  const onGetUnreadMessages = async () => {
    dispatchFeedback({ type: 'SET_IS_LOADING', payload: true });
    const data = await onLoadMessages(0, 25);
    dispatchFeedback({ type: 'SET_MESSAGES', payload: data });
    dispatchFeedback({ type: 'SET_IS_LOADING', payload: false });
  };

  const onLoadMessages = async (first, rows) => {
    const data = await FeedbackService.allUnread(first, rows);
    return data;
  };

  const onMessageSelect = event => {
    console.log(event);
    dispatchFeedback({ type: 'SET_MESSAGE_TO_SHOW', payload: event.data.message });
  };

  const onVirtualScroll = async event => {
    console.log(event);
    const data = await onLoadMessages(event.first, event.rows);
    dispatchFeedback({ type: 'SET_MESSAGES', payload: data });
  };

  const layout = children => {
    return (
      <MainLayout>
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  if (isLoading) {
    return layout(<Spinner />);
  }

  return layout(
    <Fragment>
      <Title
        title={`${resources.messages['dataflowFeedback']} `}
        subtitle={dataflowName}
        icon="info"
        iconSize="3.5rem"
      />
      <DataTable
        lazy
        loading={isLoading}
        onRowSelect={onMessageSelect}
        onVirtualScroll={onVirtualScroll}
        rows={10}
        scrollable
        scrollHeight="200px"
        selectionMode="single"
        totalRecords={messages.length}
        value={messages}
        virtualRowHeight={45}
        virtualScroll>
        <Column field="id" header="Id" loadingBody={loadingText}></Column>
        <Column field="message" header="Message" loadingBody={loadingText}></Column>
        <Column field="datetime" header="Datetime" loadingBody={loadingText}></Column>
        <Column field="read" header="Read" loadingBody={loadingText}></Column>
      </DataTable>
      {isDialogVisible && (
        <Dialog
          // className={styles.dialog}
          header={resources.messages['message']}
          onHide={onCloseDialog}
          visible={isDialogVisible}>
          <div className="p-grid p-fluid">{messageToShow}</div>
        </Dialog>
      )}
    </Fragment>
  );
});
