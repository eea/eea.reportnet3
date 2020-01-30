const getCategoryById = (categories, categoryId) => {
  return categories
    .map(e => {
      return e.id;
    })
    .indexOf(categoryId);
};

export const CategoryUtils = {
  getCategoryById
};
