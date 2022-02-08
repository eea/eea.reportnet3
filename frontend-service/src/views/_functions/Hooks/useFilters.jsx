import { useRecoilValue, useResetRecoilState } from 'recoil';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import {
  filterByState,
  filteredDataState,
  searchState,
  sortByState
} from 'views/_components/MyFilters/_functions/Stores/filtersStores';

export const useFilters = recoilId => {
  const filterBy = useRecoilValue(filterByState(recoilId));
  const filteredData = useRecoilValue(filteredDataState(recoilId));
  const sortBy = useRecoilValue(sortByState(recoilId));

  const resetFilterBy = useResetRecoilState(filterByState(recoilId));
  const resetFilteredData = useResetRecoilState(filteredDataState(recoilId));
  const resetSearchBy = useResetRecoilState(searchState(recoilId));
  const resetSortBy = useResetRecoilState(sortByState(recoilId));

  const checkIsFilter = () => {
    const isEmptyOption = item => {
      if (typeof item !== 'boolean' || item === '') {
        return isEmpty(item);
      } else {
        return isNil(item);
      }
    };

    if (isEmpty(filterBy)) {
      return false;
    }

    return Object.values(filterBy)
      .map(item => item === false || isEmptyOption(item))
      .includes(false);
  };

  const resetFiltersState = () => {
    resetFilterBy();
    resetFilteredData();
    resetSortBy();
    resetSearchBy();
  };

  return { filterBy, filteredData, isFiltered: checkIsFilter(), resetFiltersState, sortBy };
};
