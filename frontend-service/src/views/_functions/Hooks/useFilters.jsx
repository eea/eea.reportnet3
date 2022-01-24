import { useRecoilValue } from 'recoil';

import isEmpty from 'lodash/isEmpty';

import {
  filterByState,
  filteredDataState,
  sortByState
} from 'views/_components/MyFilters/_functions/Stores/filtersStores';

export const useFilters = recoilId => {
  const filterBy = useRecoilValue(filterByState(recoilId));
  const filteredData = useRecoilValue(filteredDataState(recoilId));
  const sortBy = useRecoilValue(sortByState(recoilId));

  const checkIsFilter = () => {
    if (isEmpty(filterBy)) {
      return false;
    }

    return Object.values(filterBy)
      .map(key => isEmpty(key))
      .includes(false);
  };

  return { filterBy, filteredData, isFiltered: checkIsFilter(), sortBy };
};
