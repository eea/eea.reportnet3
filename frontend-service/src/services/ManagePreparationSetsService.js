import { ManagePreparationSetsRepository } from 'repositories/ManagePreparationSetsRepository';

export const ManagePreparationSetsService = {
  getPreparationSets: async ({ dataflowId, providerId }) => {
    const response = await ManagePreparationSetsRepository.getPreparationSets({
      dataflowId,
      providerId
    });

    return response.data;
  },

  createPreparationSet: async ({ datasetName, code, dataflowId, providerId, isCreated }) => {
    return await ManagePreparationSetsRepository.createPreparationSet({
      datasetName,
      code,
      dataflowId,
      providerId,
      isCreated
    });
  },

  deletePreparationSet: async ({ id }) => await ManagePreparationSetsRepository.deletePreparationSet({ id })
};
