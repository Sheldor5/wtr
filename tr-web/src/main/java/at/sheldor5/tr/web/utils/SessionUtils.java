package at.sheldor5.tr.web.utils;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SessionUtils {

  public static HttpSession getSession() {
    return (HttpSession) FacesContext.getCurrentInstance()
        .getExternalContext().getSession(false);
  }

  public static HttpServletRequest getRequest() {
    return (HttpServletRequest) FacesContext.getCurrentInstance()
        .getExternalContext().getRequest();
  }

  public static HttpServletResponse getResponse() {
    return (HttpServletResponse) FacesContext.getCurrentInstance()
        .getExternalContext().getResponse();
  }
}