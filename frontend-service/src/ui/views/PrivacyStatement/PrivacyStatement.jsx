import React, { useContext, useEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import styles from './PrivacyStatement.module.scss';

import { routes } from 'ui/routes';
import { MainLayout } from 'ui/views/_components/Layout';
import { Title } from '../_components/Title/Title';
import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { UserCard } from './_components/UserCard';

const PrivacyStatement = withRouter(({ history }) => {
  const breadCrumbContext = useContext(BreadCrumbContext);
  const resources = useContext(ResourcesContext);

  useEffect(() => {
    breadCrumbContext.add([
      {
        label: '',
        icon: 'home',
        href: getUrl(routes.DATAFLOWS),
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      {
        label: resources.messages['privacyStatement'],
        icon: 'info',
        href: getUrl(routes.PRIVACY_STATEMENT),
        command: () => history.push(getUrl(routes.PRIVACY_STATEMENT))
      }
    ]);
  }, []);

  const toggleUserOptions = () => {
    return (
      <>
        <UserCard />
      </>
    );
  };

  const layout = children => {
    return (
      <MainLayout
        leftSideBarConfig={{
          buttons: []
        }}>
        <div className>
          <div className="rep-container">{children}</div>
        </div>
      </MainLayout>
    );
  };

  return layout(
    <div className="rep-row">
      <div className={` rep-col-12 rep-col-sm-12`}>
        <Title
          title={resources.messages['PrivacyStatementTitle']}
          icon="info"
          iconSize="4rem"
          subtitle={resources.messages['PrivacyStatementSubtitle']}
        />

        <ul>
          <li>user ID(*):</li>
          <li>First name (*):</li>
          <li>Last name (*):</li>
        </ul>

        <div className={styles.sectionMainContent}>
          <h2>Example</h2>
          <h3> Introduction</h3>
          <p>
            Any personal data you submit to the European Environment Agency (EEA) in the context of the Eionet website
            referred to above will be processed in accordance with Regulation (EU) 2018/1725 of the European Parliament
            and of the Council on the protection of natural persons with regard to the processing of personal data by
            the Union institutions, bodies, offices and agencies and on the free movement of such data. Processing
            operations are under the responsibility of the CAS1 (Networks and Partnerships) group under CAS
            (Coordination and Strategy) programme of the EEA acting as data controller, regarding the collection and
            processing of personal data. What personal data do we collect and for what purpose When you visit the
            website we do not collect any personal information and we do not link or correlate your authenticated
            session to the collected website usage statistics which are fully anonymized. See "site usage statistics"
            section for more details. We collect personal information when we create an account in the Eionet User
            Directory. This is needed in order to grant the user access to various EEA/Eionet's websites that require
            authentication. Users added to Eionet User Directory include the EEA's own staff, staff of organisations
            that EEA cooperates with, including the member organisations of the European Environment Information and
            Observation Network (Eionet) , and EU's institutions, as well as consultants working for EEA.
            Self-registration is not possible. Registration only occur by invitation of an existing member of the
            network by contacting the Eionet Helpdesk or by the National Focal Points who maintain the formal Eionet
            components (NFPs and NRCs) in their countries.
          </p>
          <h3> What personal data do we collect and for what purpose</h3>
          <p>
            When you visit the website we do not collect any personal information and we do not link or correlate your
            authenticated session to the collected website usage statistics which are fully anonymized. See "site usage
            statistics" section for more details. We collect personal information when we create an account in the
            Eionet User Directory. This is needed in order to grant the user access to various EEA/Eionet's websites
            that require authentication. Users added to Eionet User Directory include the EEA's own staff, staff of
            organisations that EEA cooperates with, including the member organisations of the European Environment
            Information and Observation Network (Eionet) , and EU's institutions, as well as consultants working for
            EEA. Self-registration is not possible. Registration only occur by invitation of an existing member of the
            network by contacting the Eionet Helpdesk or by the National Focal Points who maintain the formal Eionet
            components (NFPs and NRCs) in their countries. The personal data processed in Eionet User Directory is:
            <ul>
              <li>user ID(*):</li>
              <li>First name (*):</li>
              <li>Last name (*):</li>
              <li>Full name (native language):</li>
              <li>Reason to create the account (*):</li>
              <li>Job title</li>
              <li>E-mail(*)</li>
              <li>URL</li>
              <li>Postal address:</li>
              <li>Telephone number:</li>
              <li>Mobile telephone number:</li>
              <li>Fax number:</li>
              <li>Organisation (*):</li>
              <li>Deparment</li>
              (*) These are required fields.
            </ul>
          </p>
          <h3> Who can see your personal data</h3>
          <p>
            The Eionet User Directory is only accessible to users of the Eionet User Directory. Following personal data
            is available for authenticated users:
            <li>user ID(*):</li>
            <li>First name (*):</li>
            <li>Last name (*):</li>
            <li>Full name (native language):</li>
            <li>Reason to create the account (*):</li>
            <li>Job title</li>
            <li>E-mail(*)</li>
            <li>URL</li>
            <li>user ID(*):</li>
            <li>First name (*):</li>
            <li>Last name (*):</li>
            <li>Full name (native language):</li>
            <li>Reason to create the account (*):</li>
            <li>Job title</li>
            <li>E-mail(*)</li>
            <li>URL</li>
          </p>
        </div>
      </div>
    </div>
  );
});

export { PrivacyStatement };
