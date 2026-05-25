import { ManagePreparationSetsRepository } from 'repositories/ManagePreparationSetsRepository';

export const ManagePreparationSetsService = {
  addPreparationSet: async ({ datasetName, code, dataflowId, providerId, isCreated }) => {
    return await ManagePreparationSetsRepository.addPreparationSet({
      datasetName,
      code,
      dataflowId,
      providerId,
      isCreated
    });
  },

  createPreparationSets: async ({ dataflowId, providerId }) =>
    await ManagePreparationSetsRepository.createPreparationSets({ dataflowId, providerId }),

  deletePreparationSet: async ({ id, dataflowId }) =>
    await ManagePreparationSetsRepository.deletePreparationSet({ id, dataflowId }),

  getPreparationSets: async ({ dataflowId, providerId, code }) => {
    const response = await ManagePreparationSetsRepository.getPreparationSets({
      dataflowId,
      providerId,
      code
    });

    return response.data;
  }
};
