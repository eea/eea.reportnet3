import React from 'react';

import { storiesOf } from '../../../../../.storybook/storiesOf';
import { action } from '@storybook/addon-actions';
import { config } from 'assets/conf';
import { Button } from './Button';

storiesOf('Button', module).add('Button TO FIX', () => (
  <Button label="Push" icon={7} iconPos="right" disabled={false} onClick={action('clicked')} />
));
