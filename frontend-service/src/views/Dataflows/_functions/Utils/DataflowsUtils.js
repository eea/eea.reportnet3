const getActiveTab = (tabMenuItems, activeIndex) => {
  const { id, label } = tabMenuItems[activeIndex];

  return { activeTab: tabMenuItems[activeIndex], tabId: id, tabLabel: label };
};

export const DataflowsUtils = { getActiveTab };
