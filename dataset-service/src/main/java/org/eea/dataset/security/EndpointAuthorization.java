package org.eea.dataset.security;

import org.eea.security.authorization.ObjectAccessRoleEnum;

import static org.eea.security.authorization.ObjectAccessRoleEnum.*;

public class EndpointAuthorization {

    public static ObjectAccessRoleEnum[] getRequiredRightsForEndpoint(String endpointPath) {
        switch (endpointPath) {
            case "/dataschema/v1/getSimpleSchema/dataset/{datasetId}":
            case "/dataschema/getSimpleSchema/dataset/{datasetId}":
                return new ObjectAccessRoleEnum[]{
                    DATASET_STEWARD,
                    DATASET_CUSTODIAN,
                    DATASET_OBSERVER,
                    DATASET_LEAD_REPORTER,
                    DATASET_REPORTER_WRITE,
                    DATASET_REPORTER_READ,
                    DATASCHEMA_CUSTODIAN,
                    DATASCHEMA_STEWARD,
                    DATASCHEMA_EDITOR_WRITE,
                    EUDATASET_CUSTODIAN,
                    EUDATASET_STEWARD,
                    EUDATASET_OBSERVER,
                    DATACOLLECTION_CUSTODIAN,
                    DATACOLLECTION_STEWARD,
                    DATACOLLECTION_OBSERVER,
                    DATASET_NATIONAL_COORDINATOR,
                    TESTDATASET_CUSTODIAN,
                    TESTDATASET_STEWARD_SUPPORT,
                    TESTDATASET_STEWARD,
                    TESTDATASET_OBSERVER,
                    REFERENCEDATASET_CUSTODIAN,
                    REFERENCEDATASET_LEAD_REPORTER,
                    REFERENCEDATASET_STEWARD,
                    REFERENCEDATASET_OBSERVER};
            default: return new ObjectAccessRoleEnum[]{};
        }
    }
}
