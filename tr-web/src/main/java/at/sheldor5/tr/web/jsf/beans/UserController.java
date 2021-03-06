package at.sheldor5.tr.web.jsf.beans;

import at.sheldor5.tr.api.user.Role;
import at.sheldor5.tr.api.user.Schedule;
import at.sheldor5.tr.api.user.User;
import at.sheldor5.tr.api.user.UserMapping;
import at.sheldor5.tr.web.DataAccessLayer;
import at.sheldor5.tr.web.module.authentication.LoginModuleImpl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;

/**
 * @author Michael Palata
 * @date 02.06.2017
 */
@Named(value = "user")
@SessionScoped
public class UserController implements Serializable {

  private DataAccessLayer dataAccessLayer;

  private UserMapping userMapping;
  private User user;
  private Schedule schedule;
  private boolean admin;

  public UserController() {
    // CDI
  }

  @Inject
  public UserController(final DataAccessLayer dataAccessLayer) {
    this.dataAccessLayer = dataAccessLayer;
  }

  @PostConstruct
  public void init() {
    final String username = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getUserPrincipal().getName();
    userMapping = LoginModuleImpl.getAuthenticatedUserMapping(username);
    user = userMapping.getUser();
    admin = userMapping.getRole() == Role.ADMIN;
    schedule = dataAccessLayer.getSchedule(userMapping);
  }

  public UserMapping getUserMapping() {
    return userMapping;
  }

  public User getUser() {
    return user;
  }

  public int getId() {
    return userMapping.getId();
  }

  public String getUsername() {
    return user.getUsername();
  }

  public String getForename() {
    return user.getForename();
  }

  public String getSurname() {
    return user.getSurname();
  }

  public boolean getAdmin() {
    return admin;
  }

  public Schedule getSchedule() {
    return schedule;
  }

  public void setSchedule(final Schedule schedule) {
    this.schedule = schedule;
  }

  public void logout() throws IOException {
    final ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
    context.invalidateSession();
    context.redirect(context.getRequestContextPath() + "/");
  }
}
