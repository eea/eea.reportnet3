package org.eea.validation.persistence.data.repository;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The Class FieldExtendedRepositoryImpl.
 */
public class FieldExtendedRepositoryImpl implements FieldExtendedRepository {
  @Autowired
  private SessionFactory sessionFactory;

  /** The entity manager. */
  @PersistenceContext(name = "dataSetsEntityManagerFactory")
  private EntityManager entityManager;


  /**
   * Find completed.
   *
   * @param generatedQuery the generated query
   * @return the list
   */
  @Override
  public List<String> queryExecution(String generatedQuery) {

    Query query = entityManager.createNativeQuery(generatedQuery);
    List<String> resultList = query.getResultList();
    return resultList;

  }


  @Override
  public List<RecordValue> getDuplicatedRecordsByFields(List<String> fieldSchemaIds) {
    String queryString =
        "with table_1 as(select rv.id, (select fv.value from field_value fv where fv.id_record=rv.id and fv.id_field_schema = '5ecb97e44e781252a890e71c') AS campo_1,(select fv.value from field_value fv where fv.id_record=rv.id and fv.id_field_schema = '5ecb97e94e781252a890e71d') AS campo_2,(select fv.value from field_value fv where fv.id_record=rv.id and fv.id_field_schema = '5ecb97ed4e781252a890e71e') AS campo_3 from record_value rv) select rv.* from record_value rv where rv.id in (select t.id from (select *,count(*) over(partition by campo_2,campo_3) as N from table_1 ) as t where n>1);";
    NativeQuery<RecordValue> query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
    return query.list();
  }



}
