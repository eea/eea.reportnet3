export const getProviderColumns = (styles, resourcesContext, templates) => [
  {
    key: 'expanderColumn',
    style: { width: '3em' },
    className: styles.smallColumn
  },
  {
    key: 'jobId',
    header: resourcesContext.messages['jobId'],
    template: templates.getJobIdTemplate,
    className: styles.smallColumn
  },
  {
    key: 'dataflowId',
    header: resourcesContext.messages['dataflowId'],
    template: templates.getDataflowIdTemplate,
    className: styles.middleColumn
  },
  {
    key: 'datasetId',
    header: resourcesContext.messages['datasetId'],
    template: templates.getDatasetIdTemplate,
    className: styles.middleColumn
  },
  {
    key: 'jobType',
    header: resourcesContext.messages['jobType'],
    template: templates.getJobTypeTemplate,
    className: styles.middleColumn
  },
  {
    key: 'jobStatus',
    header: resourcesContext.messages['jobStatus'],
    template: templates.getJobStatusTemplate,
    className: styles.middleColumn
  },
  {
    key: 'dateAdded',
    header: resourcesContext.messages['dateAdded'],
    template: job => templates.getDateAddedTemplate(job, 'dateAdded'),
    className: styles.smallColumn
  },
  {
    key: 'dateStatusChanged',
    header: resourcesContext.messages['dateStatusChanged'],
    template: job => templates.getDateStatusChangedTemplate(job, 'dateStatusChanged'),
    className: styles.smallColumn
  }
];

export const getAdminCustodianColumns = (styles, resourcesContext, templates) => [
  {
    key: 'expanderColumn',
    style: { width: '3em' },
    className: styles.smallColumn
  },
  {
    key: 'jobId',
    header: resourcesContext.messages['jobId'],
    template: templates.getJobIdTemplate,
    className: styles.smallColumn
  },
  {
    key: 'fmeJobId',
    header: resourcesContext.messages['fmeJobId'],
    template: templates.getFmeJobIdTemplate,
    className: styles.middleColumn
  },
  {
    key: 'dataflowId',
    header: resourcesContext.messages['dataflowId'],
    template: templates.getDataflowIdTemplate,
    className: styles.middleColumn
  },
  {
    key: 'datasetId',
    header: resourcesContext.messages['datasetId'],
    template: templates.getDatasetIdTemplate,
    className: styles.middleColumn
  },
  {
    key: 'providerId',
    header: resourcesContext.messages['providerId'],
    template: templates.getProviderIdTemplate,
    className: styles.middleColumn
  },
  {
    key: 'creatorUsername',
    header: resourcesContext.messages['creatorUsername'],
    template: templates.getJobCreatorUsernameTemplate,
    className: styles.middleColumn
  },
  {
    key: 'jobType',
    header: resourcesContext.messages['jobType'],
    template: templates.getJobTypeTemplate,
    className: styles.middleColumn
  },
  {
    key: 'jobStatus',
    header: resourcesContext.messages['jobStatus'],
    template: templates.getJobStatusTemplate,
    className: styles.middleColumn
  },
  {
    key: 'dateAdded',
    header: resourcesContext.messages['dateAdded'],
    template: job => templates.getDateAddedTemplate(job, 'dateAdded'),
    className: styles.smallColumn
  },
  {
    key: 'dateStatusChanged',
    header: resourcesContext.messages['dateStatusChanged'],
    template: job => templates.getDateStatusChangedTemplate(job, 'dateStatusChanged'),
    className: styles.smallColumn
  }
];
export const getHistoryProviderColumns = (styles, resourcesContext, templates) => [
  {
    key: 'jobId',
    header: resourcesContext.messages['jobId'],
    template: templates.getJobIdTemplate,
    className: styles.smallColumn
  },
  {
    key: 'dataflowId',
    header: resourcesContext.messages['dataflowId'],
    template: templates.getDataflowIdTemplate,
    className: styles.middleColumn
  },
  {
    key: 'datasetId',
    header: resourcesContext.messages['datasetId'],
    template: templates.getDatasetIdTemplate,
    className: styles.middleColumn
  },
  {
    key: 'jobType',
    header: resourcesContext.messages['jobType'],
    template: templates.getJobTypeTemplate,
    className: styles.middleColumn
  },
  {
    key: 'jobStatus',
    header: resourcesContext.messages['jobStatus'],
    template: templates.getJobStatusTemplate,
    className: styles.middleColumn
  },
  {
    key: 'dateAdded',
    header: resourcesContext.messages['dateAdded'],
    template: job => templates.getDateAddedTemplate(job, 'dateAdded'),
    className: styles.smallColumn
  },
  {
    key: 'dateStatusChanged',
    header: resourcesContext.messages['dateStatusChanged'],
    template: job => templates.getDateStatusChangedTemplate(job, 'dateStatusChanged'),
    className: styles.smallColumn
  }
];

export const getHistoryAdminCustodianColumns = (styles, resourcesContext, templates) => [
  {
    key: 'jobId',
    header: resourcesContext.messages['jobId'],
    template: templates.getJobIdTemplate,
    className: styles.smallColumn
  },
  {
    key: 'fmeJobId',
    header: resourcesContext.messages['fmeJobId'],
    template: templates.getFmeJobIdTemplate,
    className: styles.middleColumn
  },
  {
    key: 'dataflowId',
    header: resourcesContext.messages['dataflowId'],
    template: templates.getDataflowIdTemplate,
    className: styles.middleColumn
  },
  {
    key: 'datasetId',
    header: resourcesContext.messages['datasetId'],
    template: templates.getDatasetIdTemplate,
    className: styles.middleColumn
  },
  {
    key: 'providerId',
    header: resourcesContext.messages['providerId'],
    template: templates.getProviderIdTemplate,
    className: styles.middleColumn
  },
  {
    key: 'creatorUsername',
    header: resourcesContext.messages['creatorUsername'],
    template: templates.getJobCreatorUsernameTemplate,
    className: styles.middleColumn
  },
  {
    key: 'jobType',
    header: resourcesContext.messages['jobType'],
    template: templates.getJobTypeTemplate,
    className: styles.middleColumn
  },
  {
    key: 'jobStatus',
    header: resourcesContext.messages['jobStatus'],
    template: templates.getJobStatusTemplate,
    className: styles.middleColumn
  },
  {
    key: 'dateAdded',
    header: resourcesContext.messages['dateAdded'],
    template: job => templates.getDateAddedTemplate(job, 'dateAdded'),
    className: styles.smallColumn
  },
  {
    key: 'dateStatusChanged',
    header: resourcesContext.messages['dateStatusChanged'],
    template: job => templates.getDateStatusChangedTemplate(job, 'dateStatusChanged'),
    className: styles.smallColumn
  }
];
