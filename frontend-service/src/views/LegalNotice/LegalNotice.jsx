import { useContext } from 'react';

import styles from './LegalNotice.module.scss';

import { PublicLayout } from 'views/_components/Layout';
import { Title } from 'views/_components/Title';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const LegalNotice = () => {
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

  const layout = children => (
    <PublicLayout>
      <div className>
        <div className="rep-container">{children}</div>
      </div>
    </PublicLayout>
  );

  return layout(
    <div className="rep-row">
      <div className="rep-col-12 rep-col-sm-12">
        <Title icon="info" iconSize="4rem" title={resourcesContext.messages['legalNotice']} />
        <div className={styles.contentMain}>
          <aside>
            <ul>
              <li>
                <a className={styles.anchorLink} href="#lnImportantNote" onClick={e => onClickAnchorLink(e)}>
                  Important note
                </a>
              </li>
              <li>
                <a className={styles.anchorLink} href="#lnAccessReuse" onClick={e => onClickAnchorLink(e)}>
                  Access and reuse of content
                </a>
              </li>
              <li>
                <a className={styles.anchorLink} href="#lnDisclaimer" onClick={e => onClickAnchorLink(e)}>
                  Disclaimer
                </a>
              </li>
              <li>
                <a className={styles.anchorLink} href="#lnFurtherInformation" onClick={e => onClickAnchorLink(e)}>
                  Further information
                </a>
              </li>
            </ul>
          </aside>
          <div className={styles.legalNoticeContent}>
            <section id="lnImportantNote">
              <h3>Important note</h3>
              <p>
                The{' '}
                <a href="https://www.eea.europa.eu/en/datahub" rel="noopener noreferrer" target="_blank">
                  European Environment Agency Datahub{' '}
                </a>
                is the preferred platform to explore and download European data sets published by the EEA.
              </p>
            </section>
            <section id="lnAccessReuse">
              <h3>Access and reuse of content in the Reportnet platform</h3>
              <p>
                Users shall refer to the access and use constraints of the datasets they are interested in. Unless
                otherwise indicated, access and re-use of publicly available content in the Reportnet platforms is
                permitted under the terms of the Creative Commons Attribution 4.0 International Public License (
                <a href="https://creativecommons.org/licenses/by/4.0/" rel="noopener noreferrer" target="_blank">
                  CC-BY 4.0
                </a>
                ), provided that the source is acknowledged. Attribution shall be provided as follows:
              </p>

              <p>
                <ul>
                  <li>
                    <strong>European Environment Agency Reportnet</strong>
                  </li>
                  <li>
                    <strong>title:</strong> content and access date
                  </li>
                  <li>
                    <strong>url:</strong> link to content in the Reportnet platform
                  </li>
                  <li>
                    <strong>license:</strong>{' '}
                    <a href="https://creativecommons.org/licenses/by/4.0/" rel="noopener noreferrer" target="_blank">
                      https://creativecommons.org/licenses/by/4.0/
                    </a>
                  </li>
                </ul>
              </p>
              <p>Attribution example:</p>

              <p>
                <ul>
                  <li>
                    <strong>European Environment Agency Reportnet</strong>
                  </li>
                  <li>
                    <strong>title:</strong> Monitoring and Classification of Bathing Waters (787) - Reporting year 2022
                    - Croatia - Accessed 2023-06-20
                  </li>
                  <li>
                    <strong>url:</strong>{' '}
                    <a href="https://reportnet.europa.eu/public/dataflow/746" rel="noopener noreferrer" target="_blank">
                      https://reportnet.europa.eu/public/dataflow/746
                    </a>
                  </li>
                  <li>
                    <strong>license:</strong>{' '}
                    <a href="https://creativecommons.org/licenses/by/4.0/" rel="noopener noreferrer" target="_blank">
                      https://creativecommons.org/licenses/by/4.0/
                    </a>
                  </li>
                </ul>
              </p>
              <p>
                Content in the Reportnet platform can be explicitly restricted from public view by the data provider.
                Only authorised users may access restricted content in the Reportnet platform. Contact{' '}
                <a href="mailto:helpdesk@reportnet.europa.eu" rel="noopener noreferrer" target="_blank">
                  helpdesk@reportnet.europa.eu
                </a>{' '}
                if you are an authorised user and require technical assistance.
              </p>
            </section>
            <section id="lnDisclaimer">
              <h3>Disclaimer</h3>
              <p>
                The European Environment Agency accepts no responsibility or liability whatsoever (including but not
                limited to any direct or consequential loss or damage that might occur to you and/or any third party)
                arising out of or in connection with the information on the Reportnet platform.
              </p>
            </section>
            <section id="lnFurtherInformation">
              <h3>Further information</h3>
              <p>
                Refer to the{' '}
                <a
                  href="https://www.eea.europa.eu/en/datahub/eea-data-policy"
                  rel="noopener noreferrer"
                  target="_blank">
                  EEA data policy
                </a>{' '}
                for further information.
                <br />
                Contact the 
                <a href="https://www.eea.europa.eu/en/about/contact-us/ask" rel="noopener noreferrer" target="_blank">
                  EEA Enquiry Service
                </a>{' '}
                if you have any questions about the re-use of content on the Reportnet platform.
              </p>
            </section>
          </div>
        </div>
      </div>
    </div>
  );
};
