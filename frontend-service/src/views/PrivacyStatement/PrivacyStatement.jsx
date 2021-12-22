import { useContext } from 'react';

import styles from './PrivacyStatement.module.scss';

import { PublicLayout } from 'views/_components/Layout';
import { Title } from 'views/_components/Title';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

const PrivacyStatement = () => {
  const resourcesContext = useContext(ResourcesContext);

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
      <div className="rep-col-12 rep-col-sm-12">
        <Title
          icon="info"
          iconSize="4rem"
          subtitle={resourcesContext.messages['privacyPolicySubtitle']}
          title={resourcesContext.messages['privacyPolicyTitle']}
        />
        <div className={styles.contentMain}>
          <aside>
            <ul>
              <li>
                <a className={styles.anchorLink} href="#gdprIntroduction" onClick={e => onClickAnchorLink(e)}>
                  Introduction
                </a>
              </li>
              <li>
                <a className={styles.anchorLink} href="#gdprDataCollectedTitle" onClick={e => onClickAnchorLink(e)}>
                  What personal data do we collect and for what purpose
                </a>
              </li>
              <li>
                <a className={styles.anchorLink} href="#gdprWhoCanSee" onClick={e => onClickAnchorLink(e)}>
                  Who can see your personal data
                </a>
              </li>
              <li>
                <a className={styles.anchorLink} href="#gdprHowCanAccess" onClick={e => onClickAnchorLink(e)}>
                  How can you access or rectify your information
                </a>
              </li>
              <li>
                <a className={styles.anchorLink} href="#gdprSiteUsage" onClick={e => onClickAnchorLink(e)}>
                  Site usage statistics and personalised experience settings
                </a>
              </li>
              <li>
                <a className={styles.anchorLink} href="#gdprHowLongStore" onClick={e => onClickAnchorLink(e)}>
                  How long do we store your personal data
                </a>
              </li>
              <li>
                <a className={styles.anchorLink} href="#gdprHowSecure" onClick={e => onClickAnchorLink(e)}>
                  How do we secure your personal data
                </a>
              </li>
              <li>
                <a className={styles.anchorLink} href="#gdprHowContact" onClick={e => onClickAnchorLink(e)}>
                  How to contact us and right to appeal
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
                Reportnet 3 requires you to login via your ‘EU Login’. ‘EU Login’ requires certain personal data such as
                the name, surname and e-mail address of the registrant. For further information, please refer to the{' '}
                <a
                  href="https://webgate.ec.europa.eu/cas/privacyStatementPopup.html"
                  rel="noopener noreferrer"
                  target="_blank">
                  privacy statement of ‘EU Login’
                </a>
                .
              </p>
            </section>
            <section id="gdprDataCollectedTitle">
              <h3>What personal data do we collect and for what purpose</h3>
              <p>
                When you visit the Reportnet 3 public page, we do not link or correlate your authenticated session to
                the collected website usage statistics. The collected statistics are fully anonymized.
              </p>
              <p>
                We collect personal information only when your EU Login account is added to the platform. This is needed
                in order to grant a user access to specific functionalities of the website that require authentication.{' '}
              </p>
              <p>Below the most common use cases for which a user needs to authenticate on the Reportnet 3 platform:</p>
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
                <a href="https://cert.europa.eu/" rel="noopener noreferrer" target="_blank">
                  https://cert.europa.eu/
                </a>
                ) which provides security services for EEA. This transactional data is also available to our Internet
                Service Provider and our cloud provider Amazon in EU (
                <a href="https://aws.amazon.com/es/privacy/" rel="noopener noreferrer" target="_blank">
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
            <section id="gdprHowCanAccess">
              <h3>How can you access or rectify your information</h3>
              <p>
                For authenticated users: if you wish to access, modify any of your personal data please log in with your
                EU login account and click on the profile icon in the top left navigation and modify some of your
                personal data.
              </p>
              <p>
                You can receive a copy of your personal data that has been submitted to the Reportnet 3 platform by
                sending an email to the Reportnet Helpdesk (
                <a href="mailto:helpdesk@reportnet.europa.eu">helpdesk@reportnet.europa.eu</a>).
              </p>
            </section>
            <section id="gdprSiteUsage">
              <h3>Site usage statistics and personalised experience settings</h3>
              <p>
                We do not store personal data in cookies. We may store your own page settings in non-personalized ways
                (e.g. your language settings).
              </p>
              <p>
                By default, the browsing experience of website visitors is tracked by EEA Matomo software in order to
                produce anonymised statistics. For example, when you visit our website, we may collect some data on your
                browsing experience such as your masked IP address (anonymized by removing the last two bytes), the web
                page you visited, when you visited and the website page you were redirected from.
              </p>
              <p>
                This information is used to gather aggregated and anonymous statistics with a view to improving our
                services and to enhance your user experience. The analytical reports generated by EEA Matomo can only be
                accessed by the EEA staff, other relevant EU institution staff or by duly authorised external
                sub-contractors, who may be required to analyse, develop and/or regularly maintain certain sites.
              </p>
              <p>
                By default, our Matomo installation respects users’ preferences and will not track visitors which have
                specified "I do not want to be tracked" in their web browsers (aka "Do not track").
              </p>
            </section>
            <section id="gdprHowLongStore">
              <h3>How long do we store your personal data</h3>
              <p>
                For authenticated users: The personal data you provide in Reportnet 3 will be kept up to seven (7)
                years. After seven (7) years from the date that you have registered to Reportnet 3, your personal data
                will be anonymised and the link to your EU login account will be removed.
              </p>
              <p>
                Additionally, you always have the right to get your account deleted from the Reportnet 3 Directory by
                sending an email to the Reportnet Helpdesk (
                <a href="mailto:helpdesk@reportnet.europa.eu">helpdesk@reportnet.europa.eu</a>).
              </p>
            </section>
            <section id="gdprHowSecure">
              <h3>How do we secure your personal data</h3>
              <p>
                Access to your personal data is subject to strict security controls like encryption and access control.
                We do not share your personal data with third parties without your prior consent.
              </p>
              <p>
                The functioning of the servers and databases containing the personal data is compliant with the EEA's
                Information Security Policy and the provisions established by the EEA's Information Security Officer.
              </p>
            </section>
            <section id="gdprHowContact">
              <h3>How to contact us and right to appeal</h3>
              <p>
                You may contact the EEA’s Data Protection Officer (DPO) in case of any difficulties relating to the
                processing of your data at the following email address:{' '}
                <a href="mailto:dpo@eea.europa.eu">dpo@eea.europa.eu</a>.
              </p>
              <p>
                You are entitled to have recourse at any time to the European Data Protection Supervisor (
                <a href="https://edps.europa.eu" rel="noopener noreferrer" target="_blank">
                  https://edps.europa.eu
                </a>
                ; <a href="mailto:edps@edps.europa.eu">edps@edps.europa.eu</a>) if you consider that your rights under
                Regulation (EU) 2018/1725 have been infringed as a result of the processing of your personal data by the
                EEA.
              </p>
            </section>
          </div>
        </div>
      </div>
    </div>
  );
};

export { PrivacyStatement };
