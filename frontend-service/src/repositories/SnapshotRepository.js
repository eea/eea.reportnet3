import { SnapshotConfig } from './config/SnapshotConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const SnapshotRepository = {
  getAllDesigner: async datasetSchemaId =>
    await HTTPRequester.get({ url: getUrl(SnapshotConfig.getAllDesigner, { datasetSchemaId }) }),

  getAllReporter: async datasetId =>
    await HTTPRequester.get({ url: getUrl(SnapshotConfig.getAllReporter, { datasetId }) }),

  createDesigner: async (datasetId, datasetSchemaId, description) =>
    await HTTPRequester.post({
      url: getUrl(SnapshotConfig.createDesigner, { datasetId, datasetSchemaId, description }),
      data: { description }
    }),

  createReporter: async (datasetId, description, isReleased) =>
    await HTTPRequester.post({
      url: getUrl(SnapshotConfig.createReporter, { datasetId }),
      data: { description, released: isReleased }
    }),

  deleteDesigner: async (datasetSchemaId, snapshotId) =>
    await HTTPRequester.delete({ url: getUrl(SnapshotConfig.deleteDesigner, { datasetSchemaId, snapshotId }) }),

  deleteReporter: async (datasetId, snapshotId) =>
    await HTTPRequester.delete({ url: getUrl(SnapshotConfig.deleteReporter, { datasetId, snapshotId }) }),

  restoreDesigner: async (datasetSchemaId, snapshotId) =>
    await HTTPRequester.post({
      url: getUrl(SnapshotConfig.restoreDesigner, { datasetSchemaId, snapshotId }),
      data: { snapshotId }
    }),

  restoreReporter: async (dataflowId, datasetId, snapshotId) =>
    await HTTPRequester.post({
      url: getUrl(SnapshotConfig.restoreReporter, { dataflowId, datasetId, snapshotId }),
      data: { snapshotId }
    }),

  release: async (dataflowId, dataProviderId, restrictFromPublic) =>
    await HTTPRequester.post({
      url: getUrl(SnapshotConfig.release, { dataflowId, dataProviderId, restrictFromPublic })
    }),

  update: async (
    dataflowId,
    dataProviderId,
    restrictFromPublic // TODO ADD REAL ENDPOINT
  ) =>
    await HTTPRequester.update({
      url: getUrl(SnapshotConfig.update, { dataflowId, dataProviderId, restrictFromPublic })
    })
};
