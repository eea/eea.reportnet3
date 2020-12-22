import React, { useContext, useEffect } from 'react';
import { withRouter } from 'react-router-dom';

import DOMPurify from 'dompurify';

import styles from './PrivacyStatement.module.scss';

import { PublicLayout } from 'ui/views/_components/Layout';
import { Title } from 'ui/views/_components/Title';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const PrivacyStatement = withRouter(({ history }) => {
  const resources = useContext(ResourcesContext);

  const onClickAnchorLink = e => {
    e.preventDefault();
    const anchorLink = e.target;
    const anchorTarget = document.querySelector(`#${anchorLink.href.split('#')[1]}`);
    const header = document.querySelector('#header');
    let scrollHeight = anchorTarget.getBoundingClientRect().top;

    if (header.clientHeight === 180) {
      scrollHeight = anchorTarget.getBoundingClientRect().top - 100;
      if (window.innerWidth <= 768) {
        scrollHeight = scrollHeight - 100;
      }
    }
    window.scroll({
      top: scrollHeight,
      left: 0,
      behavior: 'smooth'
    });
  };

  const layout = children => {
    return (
      <PublicLayout>
        <div className>
          <div className="rep-container">{children}</div>
        </div>
      </PublicLayout>
    );
  };

  return layout(
    <div className="rep-row">
      <div className={` rep-col-12 rep-col-sm-12`}>
        <Title
          title={resources.messages['privacyPolicyTitle']}
          icon="info"
          iconSize="4rem"
          subtitle={resources.messages['privacyPolicySubtitle']}
        />
        <div className={styles.contentMain}>
          <aside>
            <ul>
              <li>
                <a className={styles.anchorLink} onClick={e => onClickAnchorLink(e)} href="#gdprIntroduction">
                  {resources.messages['gdprIntroduction']}
                </a>
              </li>
              <li>
                <a className={styles.anchorLink} onClick={e => onClickAnchorLink(e)} href="#gdprDataCollectedTitle">
                  {resources.messages['gdprDataCollectedTitle']}
                </a>
              </li>
              <li>
                <a className={styles.anchorLink} onClick={e => onClickAnchorLink(e)} href="#gdprWhoCanSee">
                  {resources.messages['gdprWhoCanSee']}
                </a>
              </li>
            </ul>
          </aside>
          <div className={styles.privacyStatementContent}>
            <section id="gdprIntroduction">
              <h3>Introduction</h3>
              <p>
                Any personal data you submit to the European Environment Agency (EEA) in the context of the Reportnet
                3.0 platform will be processed in accordance with Regulation (EU) 2018/1725 of the European Parliament
                and of the Council on the protection of natural persons with regard to the processing of personal data
                by the Union institutions, bodies, offices and agencies and on the free movement of such data.{' '}
              </p>
              <p>
                With regard to the personal data collected for logging in purposes, processing operations are under the
                responsibility of the DIS1 (Information Systems and ICT) group under DIS (Data and Information Services)
                programme of the EEA acting as data controller, regarding the collection and processing of personal
                data.
              </p>
              <p>
                This privacy statement explains the reason for the processing of your personal data, the way we collect,
                handle and ensure protection of all personal data provided, how that information is used and what rights
                you have in relation to your personal data. It also specifies the contact details of the responsible
                Data Controller with whom you may exercise your rights, the Data Protection Officer and the European
                Data Protection Supervisor.
              </p>
              <p>
                Reportnet 3.0 requires you to login via your ‘EU Login’. ‘EU Login’ requires certain personal data such
                as the name, surname and e-mail address of the registrant. For further information, please refer to the{' '}
                <a href="https://webgate.ec.europa.eu/cas/privacyStatementPopup.html" target="_blank">
                  privacy statement of ‘EU Login’
                </a>
                .
              </p>
            </section>
            <section id="gdprDataCollectedTitle">
              <h3>What personal data do we collect and for what purpose</h3>
              <p>
                When you visit the Reportnet 3.0 public page, we do not link or correlate your authenticated session to
                the collected website usage statistics. The collected statistics are fully anonymized.
              </p>
              <p>
                We collect personal information only when your EU Login account is added to the platform. This is needed
                in order to grant a user access to specific functionalities of the website that require authentication.{' '}
              </p>
              <p>
                Below the most common use cases for which a user needs to authenticate on the Reportnet 3.0 platform:
              </p>
              <p>
                <ul>
                  <li>
                    A user is going to create a dataflow, assign access rights, manage the dataflow and design the
                    schema of a legal delivery;
                  </li>
                  <li>
                    A user is going to define and manage legislative instruments and ensure the quality of the reported
                    data and create the EU dataset;
                  </li>
                  <li>A user is going to import data and submit the data collection to a legal delivery;</li>
                  <li>
                    A user is going to resolve any technical problems related to the reporting process and support the
                    countries in the reporting process.
                  </li>
                </ul>
              </p>
              <p>
                We process personal data (transactional data) such as anonymized IP-address, browser version and other
                device information that is necessary to securely deliver web pages to your internet client. This
                transactional data is also processed by personnel at EEA and at CERT-EU (
                <a href="https://cert.europa.eu/" target="_blank">
                  https://cert.europa.eu/
                </a>
                ) which provides security services for EEA. This transactional data is also available to our Internet
                Service Provider and our cloud provider Amazon in EU (
                <a href="https://aws.amazon.com/es/privacy/" target="_blank">
                  see their privacy statement
                </a>
                ).
              </p>
            </section>
            <section id="gdprWhoCanSee">
              <h3>Who can see your personal data</h3>
              <p>
                A dedicated number of authorised users within the EEA are authorised to see all history on each content
                to allow the team to rectify web content, contact initial contributor or roll-back to a previous version
                in case of need.
              </p>
              <p>Personal data is not shared with third parties for direct marketing purposes.</p>
              <p>
                There are no third country transfers. We store your data within the European Economic Area/European
                Union.
              </p>
            </section>
          </div>
        </div>
      </div>
    </div>
  );
});

export { PrivacyStatement };
