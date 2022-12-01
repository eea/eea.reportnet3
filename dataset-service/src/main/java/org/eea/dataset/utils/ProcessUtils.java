package org.eea.dataset.utils;

import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ProcessUtils {

    public ProcessVO createProcessVOForRelease(Long dataflowId, Long datasetId, String processId) {
        ProcessVO processVO = new ProcessVO();
        processVO.setDataflowId(dataflowId);
        processVO.setDatasetId(datasetId);
        processVO.setStatus(ProcessStatusEnum.IN_QUEUE.toString());
        processVO.setUser(SecurityContextHolder.getContext().getAuthentication().getName());
        processVO.setPriority(1);
        processVO.setProcessType(ProcessTypeEnum.RELEASE_SNAPSHOT.toString());
        processVO.setProcessId(processId);
        processVO.setQueuedDate(new Date());
        return processVO;
    }
}
