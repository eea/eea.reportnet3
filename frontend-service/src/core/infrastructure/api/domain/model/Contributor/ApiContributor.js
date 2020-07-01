import { ContributorConfig } from 'conf/domain/model/Contributor';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

const apiContributor = {
  allEditors: async dataflowId => {
    let allEditorsURL = getUrl(ContributorConfig.allEditors, { dataflowId });
    console.log('allEditorsURL', allEditorsURL);

    const response = await HTTPRequester.get({
      url: getUrl(ContributorConfig.allEditors, {
        dataflowId
      })
    });

    return response.data;
  },

  allReporters: async (dataflowId, dataProviderId) => {
    let allReportersURL = getUrl(ContributorConfig.allReporters, { dataflowId, dataProviderId });
    console.log('allReportersURL', allReportersURL);

    const response = await HTTPRequester.get({
      url: getUrl(ContributorConfig.allReporters, {
        dataflowId,
        dataProviderId
      })
    });

    return response.data;
  },

  deleteEditor: async (account, dataflowId) => {
    let deleteEditorURL = getUrl(ContributorConfig.deleteEditor, { dataflowId });
    console.log('deleteEditorURL', deleteEditorURL);
    console.log('deleteEditor body', account);

    const response = await HTTPRequester.delete({
      url: getUrl(ContributorConfig.deleteEditor, { dataflowId }),
      data: {
        account: account
      }
    });
    return response;
  },

  deleteReporter: async (account, dataflowId, dataProviderId) => {
    let deleteReporterURL = getUrl(ContributorConfig.deleteEditor, { dataflowId, dataProviderId });
    console.log('deleteReporterURL', deleteReporterURL);
    console.log('deleteReporter body', account);

    const response = await HTTPRequester.delete({
      url: getUrl(ContributorConfig.deleteReporter, { dataflowId, dataProviderId }),
      data: {
        account: account
      }
    });
    return response;
  },

  updateEditor: async (editor, dataflowId) => {
    let updateEditorURL = getUrl(ContributorConfig.updateEditor, { dataflowId });
    console.log('updateEditorURL', updateEditorURL);
    console.log('updateEditor body', editor.account, Boolean(editor.writePermission));

    const response = await HTTPRequester.update({
      url: getUrl(ContributorConfig.updateEditor, { dataflowId }),
      data: {
        account: editor.account,
        writePermission: Boolean(editor.writePermission)
      }
    });
    return response;
  },

  updateReporter: async (reporter, dataflowId, dataProviderId) => {
    let updateReporterURL = getUrl(ContributorConfig.updateReporter, { dataflowId, dataProviderId });
    console.log('updateReporterURL', updateReporterURL);
    console.log('updateReporter body', reporter.account, Boolean(reporter.writePermission));

    const response = await HTTPRequester.update({
      url: getUrl(ContributorConfig.updateReporter, { dataflowId, dataProviderId }),
      data: {
        account: reporter.account,
        writePermission: Boolean(reporter.writePermission)
      }
    });
    return response;
  }
};

export { apiContributor };
