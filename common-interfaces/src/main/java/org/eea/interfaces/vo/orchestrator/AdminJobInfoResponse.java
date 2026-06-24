package org.eea.interfaces.vo.orchestrator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.locks.Lock;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AdminJobInfoResponse {

  String info;
  List<Long> requestedJobIds;
  // TODO: add locks for the related job
  List<Lock> locks;

  AdminJobInfoAnalytics adminJobInfoAnalytics;
  List<AdminJobInfoVO> adminJobInfoVOs;

}
