package org.eea.security.authorization;

import java.lang.annotation.Inherited;
import org.springframework.security.access.prepost.PreAuthorize;

@Inherited
@PreAuthorize("@secondLevelAuthorizationService.checkObjectAccess(dataflowId(),'DATAFLOW_PROVIDER','DATAFLOW_REQUESTOR')")
public @interface DataflowProvider {

  String dataflowId();
}
