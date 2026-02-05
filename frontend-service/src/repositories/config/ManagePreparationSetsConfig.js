export const ManagePreparationSetsConfig = {
  addPreparationSet: '/dataset/preparations',
  createPreparationSets: '/dataset/createAllEligiblePreparationSets?dataflowId={:dataflowId}&providerId={:providerId}',
  deletePreparationSet: '/dataset/preparations/{:id}',
  getPreparationSets: '/dataset/preparations?dataflowId={:dataflowId}&providerId={:providerId}&code={:code}'
};
