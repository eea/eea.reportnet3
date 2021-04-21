import { storiesOf } from '../../../../../.storybook/storiesOf';
import { action } from '@storybook/addon-actions';
import { Checkbox } from './Checkbox';

storiesOf('Checkbox', module).add('Default', () => <Checkbox label="Default" />);
