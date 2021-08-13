import { storiesOf } from '../../../../.storybook/storiesOf';
import { action } from '@storybook/addon-actions';
import { ColorPicker } from './ColorPicker';

storiesOf('Color Picker', module).add('Default', () => (
  <ColorPicker className={`styles.colorPicker`} onChange={action('Selected color')} value="#99CC33" />
));
