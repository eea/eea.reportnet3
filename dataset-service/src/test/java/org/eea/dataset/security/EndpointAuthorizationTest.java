package org.eea.dataset.security;

import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class EndpointAuthorizationTest {

    private final String endpointPath;
    private final ObjectAccessRoleEnum[] expectedRoles;

    public EndpointAuthorizationTest(String endpointPath, ObjectAccessRoleEnum[] expectedRoles) {
        this.endpointPath = endpointPath;
        this.expectedRoles = expectedRoles;
    }

    @Parameters(name = "{index}: endpoint={0} → expected roles count={1}")
    public static Collection<Object[]> data() {
        ObjectAccessRoleEnum[] fullRoles = {
                ObjectAccessRoleEnum.DATASET_STEWARD,
                ObjectAccessRoleEnum.DATASET_CUSTODIAN,
                ObjectAccessRoleEnum.DATASET_OBSERVER,
                ObjectAccessRoleEnum.DATASET_LEAD_REPORTER,
                ObjectAccessRoleEnum.DATASET_REPORTER_WRITE,
                ObjectAccessRoleEnum.DATASET_REPORTER_READ,
                ObjectAccessRoleEnum.DATASCHEMA_CUSTODIAN,
                ObjectAccessRoleEnum.DATASCHEMA_STEWARD,
                ObjectAccessRoleEnum.DATASCHEMA_EDITOR_WRITE,
                ObjectAccessRoleEnum.EUDATASET_CUSTODIAN,
                ObjectAccessRoleEnum.EUDATASET_STEWARD,
                ObjectAccessRoleEnum.EUDATASET_OBSERVER,
                ObjectAccessRoleEnum.DATACOLLECTION_CUSTODIAN,
                ObjectAccessRoleEnum.DATACOLLECTION_STEWARD,
                ObjectAccessRoleEnum.DATACOLLECTION_OBSERVER,
                ObjectAccessRoleEnum.DATASET_NATIONAL_COORDINATOR,
                ObjectAccessRoleEnum.TESTDATASET_CUSTODIAN,
                ObjectAccessRoleEnum.TESTDATASET_STEWARD_SUPPORT,
                ObjectAccessRoleEnum.TESTDATASET_STEWARD,
                ObjectAccessRoleEnum.TESTDATASET_OBSERVER,
                ObjectAccessRoleEnum.REFERENCEDATASET_CUSTODIAN,
                ObjectAccessRoleEnum.REFERENCEDATASET_LEAD_REPORTER,
                ObjectAccessRoleEnum.REFERENCEDATASET_STEWARD,
                ObjectAccessRoleEnum.REFERENCEDATASET_OBSERVER
        };

        return Arrays.asList(new Object[][]{
                // Matching endpoints - should return roles list.
                {"/dataschema/v1/getSimpleSchema/dataset/{datasetId}", fullRoles},
                {"/dataschema/getSimpleSchema/dataset/{datasetId}",      fullRoles},

                // Non-matching endpoints - should return empty array
                {"/dataschema/v1/getSimpleSchema/dataset/123", new ObjectAccessRoleEnum[]{}},
                {"/dataschema/getSimpleSchema/dataset/abc",    new ObjectAccessRoleEnum[]{}},
                {"/other/endpoint",                            new ObjectAccessRoleEnum[]{}},
                {"",                                           new ObjectAccessRoleEnum[]{}}
        });
    }

    @Test
    public void testGetRequiredRightsForEndpoint() {
        ObjectAccessRoleEnum[] actual = EndpointAuthorization.getRequiredRightsForEndpoint(endpointPath);

        if (expectedRoles.length == 0) {
            assertEquals(0, actual.length);
        } else {
            assertArrayEquals(expectedRoles, actual);
        }
    }
}