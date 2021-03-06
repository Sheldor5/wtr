package at.sheldor5.tr.persistence.provider;

import at.sheldor5.tr.api.project.Project;
import at.sheldor5.tr.api.user.UserMapping;
import at.sheldor5.tr.persistence.EntityManagerHelper;
import at.sheldor5.tr.persistence.identifier.AbstractIdentifier;
import at.sheldor5.tr.persistence.mappings.UserProjectMapping;
import at.sheldor5.tr.persistence.utils.QueryUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Palata
 * @date 05.06.2017
 */
public class UserProjectMappingProvider extends GenericProvider<UserProjectMapping, Integer> {

  private static final Class<UserProjectMapping> entityClass = UserProjectMapping.class;
  private static final Class<Integer> entityIdentifierClass = Integer.class;
  private static final String entityIdentifierName = "id";

  private static final AbstractIdentifier<UserProjectMapping, Integer> IDENTIFIER =
      new AbstractIdentifier<UserProjectMapping, Integer>(entityClass, entityIdentifierClass, entityIdentifierName) {
        @Override
        public Integer getIdentifier(final UserProjectMapping project) {
          return project == null ? null : project.getId();
        }
      };

  public UserProjectMappingProvider() {
    this(EntityManagerHelper.createEntityManager());
  }

  public UserProjectMappingProvider(final EntityManager entityManager) {
    super(entityManager, IDENTIFIER);
  }

  @Override
  public boolean exists(final UserProjectMapping entity) {
    TypedQuery<Long> countQuery =
        QueryUtils.countByFields(entityManager, UserProjectMapping.class,
            "project", Project.class, entity.getProject(),
            "userMapping", UserMapping.class, entity.getUserMapping(), true);
    return countQuery.getSingleResult() > 0;
  }

  public List<Project> getProjects(final UserMapping userMapping) {
    List<UserProjectMapping> userProjectMappings = getUserProjectMappings(userMapping);

    final List<Project> projects = new ArrayList<>();
    if (userProjectMappings != null) {
      for (final UserProjectMapping userProjectMapping : userProjectMappings) {
        projects.add(userProjectMapping.getProject());
      }
    }

    return projects;
  }

  public List<UserProjectMapping> getUserProjectMappings(final UserMapping userMapping) {
    if (userMapping == null) {
      return new ArrayList<>();
    }

    List<UserProjectMapping> result = new ArrayList<>();

    final TypedQuery<UserProjectMapping> findByUserMapping = QueryUtils.findByField(entityManager,
            UserProjectMapping.class, "userMapping", UserMapping.class, userMapping);

    EntityTransaction transaction = entityManager.getTransaction();
    transaction.begin();

    try {
      result = findByUserMapping.getResultList();
      transaction.commit();
    } catch (final Exception e) {
      transaction.rollback();
      e.printStackTrace();
    }

    return result;
  }
}
