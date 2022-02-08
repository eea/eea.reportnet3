import { useEffect } from 'react';
import { useRecoilState, useSetRecoilState } from 'recoil';

import uniq from 'lodash/uniq';

import { filterByAllKeys } from 'views/_components/Filters/_functions/Stores/filterKeysStore';
import { filterByStore } from 'views/_components/Filters/_functions/Stores/filterStore';

export const useFilters = ({ hasCustomSort, keyStore, onFilterData, option, recoilId }) => {
  const setFilterByAllKeys = useSetRecoilState(filterByAllKeys(recoilId));
  const setFilterByKeyByType = useSetRecoilState(keyStore(recoilId));

  const [filterBy, setFilterBy] = useRecoilState(filterByStore(`${option.key}_${recoilId}`));

  useEffect(() => {
    setFilterByKeyByType(prevState => ({ keys: uniq([...prevState.keys, option.key]), nestedKey: option?.nestedKey }));
    setFilterByAllKeys(prevState => uniq([...prevState, option.key]));
  }, [recoilId]);

  const onFilter = async value => {
    setFilterBy({ [option.key]: value });

    if (!hasCustomSort) {
      await onFilterData({ key: option.key, value, type: option.type });
    }
  };

  return { filterBy, onFilter };
};
