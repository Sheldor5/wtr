package at.sheldor5.tr.core.auth;

import at.sheldor5.tr.api.user.User;
import at.sheldor5.tr.api.utils.GlobalProperties;
import at.sheldor5.tr.auth.db.DatabaseAuthentication;
import at.sheldor5.tr.auth.ldap.LdapAuthentication;
import at.sheldor5.tr.core.persistence.DatabaseConnection;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

public class AuthenticationManagerTest {

  private static final File PROPERTIES_DB = new File("db.properties");
  private static final File PROPERTIES_LDAP = new File("ldap.properties");

  private static final String FORENAME = "Time";
  private static final String SURNAME = "Recorder";

  private static final String LDAP_USER = "time-recorder";
  private static final String LDAP_PASS = "password";

  private static final String DB_USER = "db_test";
  private static final String DB_PASS = "db_pass";
  private static final String DB_FORENAME = "Database";
  private static final String DB_SURNAME = "User";

  private static AuthenticationManager manager;

  private static DatabaseAuthentication db;
  private static LdapAuthentication ldap;

  @BeforeClass
  public static void init() throws IOException, SQLException {
    GlobalProperties.load(PROPERTIES_DB);
    GlobalProperties.load(PROPERTIES_LDAP);

    SQLServerDataSource dataSource = new SQLServerDataSource();
    dataSource.setURL(DatabaseConnection.getJDBCUrl());

    manager = new AuthenticationManager();
    manager.setDataSource(dataSource);

    db = new DatabaseAuthentication();
    db.setDataSource(dataSource);
    db.initialize();

    ldap = new LdapAuthentication();
    ldap.initialize();

    final User user = db.getUser(DB_USER, DB_PASS);
    if (user == null) {
      db.addUser(new User(DB_USER, DB_FORENAME, DB_SURNAME), DB_PASS);
    }

    Assume.assumeNotNull(db.getUser(DB_USER, DB_PASS));

    manager.addPlugin(db);
    manager.addPlugin(ldap);

    manager.initialize();
  }

  @Test
  public void test_ldap() {
    final User user = manager.getUser(LDAP_USER, LDAP_PASS);

    Assert.assertNotNull(user);
    Assert.assertEquals(FORENAME, user.getForename());
    Assert.assertEquals(SURNAME, user.getSurname());
    Assert.assertNotNull(user.getUuid());
  }

  @Test
  public void test_db() {
    final User user = manager.getUser(DB_USER, DB_PASS);

    Assert.assertNotNull(user);
    Assert.assertEquals(DB_FORENAME, user.getForename());
    Assert.assertEquals(DB_SURNAME, user.getSurname());
    Assert.assertNotNull(user.getUuid());
  }
}