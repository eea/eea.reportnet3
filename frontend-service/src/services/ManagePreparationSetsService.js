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

  deletePreparationSet: async ({ id }) => await ManagePreparationSetsRepository.deletePreparationSet({ id }),

  getPreparationSets: async ({ dataflowId, providerId, code }) => {
    const response = await ManagePreparationSetsRepository.getPreparationSets({
      dataflowId,
      providerId,
      code
    });

    return response.data;
  }
};
