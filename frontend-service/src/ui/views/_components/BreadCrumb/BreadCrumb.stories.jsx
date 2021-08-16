import { config } from 'conf';

import { storiesOf } from '../../../../../.storybook/storiesOf';
import { action } from '@storybook/addon-actions';
import { BreadCrumb } from './BreadCrumb';

const home = {
  icon: config.icons['home'],
  command: action('Go home!')
};

storiesOf('BreadCrumb', module).add('Default', () => (
  <BreadCrumb
    home={home}
    model={[
      {
        label: 'Home',
        command: action('Go Home!')
      },
      {
        label: 'Dataflow',
        command: action('Go to Dataflow!')
      },
      {
        label: 'Dataset designer',
        command: action('Go to designer!')
      }
    ]}
  />
));
