export const AddOrganizationsConfig = {
  createProvider: '/representative/provider/create',
  getOrganizations:
    '/representative/dataProvider/?pageNum={:pageNum}&pageSize={:numberRows}&asc={:sortOrder}&sortedColumn={:sortField}&providerCode={:providerCode}&groupId={:groupId}&label={:label}',
  getProviderGroups: '/representative/dataProviderGroups',
  updateProvider: '/representative/provider/update'
};
