import ReactDOM from 'react-dom';
import { SessionExpireDialog } from 'views/_components/SessionExpireDialog/SessionExpireDialog';
import { ResourcesProvider } from 'views/_functions/Providers/ResourcesProvider';
import { RecoilRoot } from 'recoil';

export const showSessionExpiredDialog = () => {
  ReactDOM.render(
    <ResourcesProvider>
      <RecoilRoot>
        <SessionExpireDialog visible={true} />
      </RecoilRoot>
    </ResourcesProvider>,
    document.getElementById('root')
  );
};
