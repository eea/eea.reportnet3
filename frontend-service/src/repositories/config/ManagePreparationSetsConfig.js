export const ManagePreparationSetsConfig = {
  addPreparationSet: '/dataset/preparations',
  createPreparationSets: '/dataset/createAllEligiblePreparationSets?dataflowId={:dataflowId}&providerId={:providerId}',
  deletePreparationSet: '/dataset/preparations/{:id}?dataflowId={:dataflowId}',
  getPreparationSets: '/dataset/preparations?dataflowId={:dataflowId}&providerId={:providerId}&code={:code}'
};
