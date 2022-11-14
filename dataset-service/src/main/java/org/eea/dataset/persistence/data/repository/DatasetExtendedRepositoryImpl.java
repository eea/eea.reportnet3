package org.eea.dataset.persistence.data.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.eea.dataset.service.model.PgStatActivity;
import org.eea.interfaces.vo.dataset.PgStatActivityVO;
import org.hibernate.Session;
import org.postgresql.util.PGInterval;
import org.springframework.data.repository.query.Param;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class DatasetExtendedRepositoryImpl.
 */
public class DatasetExtendedRepositoryImpl implements DatasetExtendedRepository {

  /** The entity manager. */
  @PersistenceContext
  private EntityManager entityManager;

  /** The Constant QUERY_DROP_SCHEMA. */
  private static final String QUERY_DROP_SCHEMA = "DROP SCHEMA ?1";

  /** pg_stat_activity query */
  private static final String PG_STAT_ACTIVITY_QUERY = "SELECT pid, usename, application_name, query FROM pg_stat_activity WHERE state != 'idle' AND query NOT ILIKE '%pg_stat_activity%' ORDER BY query_start DESC;";


  /**
   * Delete schema.
   *
   * @param schema the schema
   */
  @Override
  public void deleteSchema(@Param("schema") String schema) {
    Query query = entityManager.createNativeQuery(QUERY_DROP_SCHEMA);
    query.setParameter(1, schema);
    query.executeUpdate();
  }

  /**
   * Get pg_stat_activity information
   * @return
   */
  @Override
  public List<PgStatActivityVO> getPgStatActivityResults() {
    Session session = (Session) entityManager.getDelegate();
    return session.doReturningWork(conn -> {
      try (PreparedStatement stmt = conn.prepareStatement(PG_STAT_ACTIVITY_QUERY)) {
        ResultSet rs = stmt.executeQuery();
        List<PgStatActivityVO> pgStatActivityList = new ArrayList<>();
        while (rs.next()) {
          PgStatActivityVO pgStatActivity = new PgStatActivityVO();
          pgStatActivity.setPid(rs.getString("pid"));
          pgStatActivity.setUserName(rs.getString("usename"));
          pgStatActivity.setApplicationName(rs.getString("application_name"));
          pgStatActivity.setQuery(rs.getString("query"));
          pgStatActivityList.add(pgStatActivity);
        }
        return pgStatActivityList;
      }
    });
  }

}
