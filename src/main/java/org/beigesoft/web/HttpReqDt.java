/*
BSD 2-Clause License

Copyright (c) 2019, Beigesoft™
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.beigesoft.web;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.beigesoft.mdl.IReqDt;
import org.beigesoft.mdl.Cokie;

/**
 * <p>Wrapper (adapter) of HttpServletRequest/HttpServletResponse.</p>
 *
 * @author Yury Demidenko
 */
public class HttpReqDt implements IReqDt {

  /**
   * <p>Http Servlet Request to adapt.</p>
   **/
  private final HttpServletRequest httpReq;

  /**
   * <p>Http Servlet Request to adapt.</p>
   **/
  private final HttpServletResponse httpResp;

  /**
   * <p>Only constructor.</p>
   * @param pHttpReq reference
   * @param pHttpResp reference
   **/
  public HttpReqDt(final HttpServletRequest pHttpReq,
    final HttpServletResponse pHttpResp) {
    this.httpReq = pHttpReq;
    this.httpResp = pHttpResp;
  }

  /**
   * <p>Getter for parameter.</p>
   * @param pParamName Parameter Name
   * @return parameter
   **/
  @Override
  public final String getParam(final String pParamName) {
    return httpReq.getParameter(pParamName);
  }

  /**
   * <p>Getter for parameter with multiply values.</p>
   * @param pParamName Parameter Name
   * @return parameter values
   **/
  @Override
  public final String[] getParamVls(final String pParamName) {
    return httpReq.getParameterValues(pParamName);
  }

  /**
   * <p>Getter for Parameters Map.</p>
   * @return parameters map
   **/
  @Override
  public final Map<String, String[]> getParamMap() {
    return httpReq.getParameterMap();
  }

  /**
   * <p>Getter of user name.</p>
   * @return User name if he/she logged
   **/
  @Override
  public final String getUsrNm() {
    if (httpReq.getUserPrincipal() != null) {
      return httpReq.getUserPrincipal().getName();
    }
    return null;
  }

  /**
   * <p>Getter for context attribute.</p>
   * @param pCtxAttrNm CtxAttr name
   * @return CtxAttr
   **/
  @Override
  public final Object getCtxAttr(final String pCtxAttrNm) {
    return httpReq.getServletContext().getAttribute(pCtxAttrNm);
  }

  /**
   * <p>Setter for context attribute.</p>
   * @param pCtxAttrNm CtxAttr name
   * @param pCtxAttr reference
   **/
  @Override
  public final void setCtxAttr(final String pCtxAttrNm,
    final Object pCtxAttr) {
    httpReq.getServletContext().setAttribute(pCtxAttrNm, pCtxAttr);
  }

  /**
   * <p>Removes context attribute.</p>
   * @param pCtxAttrNm CtxAttr name
   **/
  @Override
  public final void remCtxAttr(final String pCtxAttrNm) {
    httpReq.getServletContext().removeAttribute(pCtxAttrNm);
  }

  /**
   * <p>Getter for attribute.</p>
   * @param pAttrName Attribute name
   * @return Attribute
   **/
  @Override
  public final Object getAttr(final String pAttrName) {
    return httpReq.getAttribute(pAttrName);
  }

  /**
   * <p>Setter for attribute.</p>
   * @param pAttrName Attribute name
   * @param pAttribute reference
   **/
  @Override
  public final void setAttr(final String pAttrName,
    final Object pAttribute) {
    httpReq.setAttribute(pAttrName, pAttribute);
  }

  /**
   * <p>Removes attribute.</p>
   * @param pAttrName Attribute name
   **/
  @Override
  public final void remAttr(final String pAttrName) {
    httpReq.removeAttribute(pAttrName);
  }

  /**
   * <p>Getter for cookies.</p>
   * @return Cokie[]
   **/
  public final Cokie[] getCookies() {
    if (this.httpReq.getCookies() == null) {
      return null;
    }
    Cokie[] cokies = new Cokie[this.httpReq.getCookies().length];
    int i = 0;
    for (Cookie q : this.httpReq.getCookies()) {
      Cokie c = new Cokie();
      c.setNme(q.getName());
      c.setVal(q.getValue());
      cokies[i++] = c;
    }
    return cokies;
  }

  /**
   * <p>Get cookie value by name.</p>
   * @param pName Name
   * @return cookie value or null
   **/
  @Override
  public final String getCookVl(final String pName) {
    if (this.httpReq.getCookies() == null) {
      return null;
    }
    for (Cookie co : this.httpReq.getCookies()) {
      if (co.getName().equals(pName)) {
        return co.getValue();
      }
    }
    return null;
  }

  /**
   * <p>Set(add/change) cookie value.</p>
   * @param pName Name
   * @param pValue Value
   **/
  @Override
  public final void setCookVl(final String pName, final String pValue) {
    Cookie cookie = null;
    if (this.httpReq.getCookies() != null) {
      for (Cookie co : this.httpReq.getCookies()) {
        if (co.getName().equals(pName)) {
          cookie = co;
          cookie.setValue(pValue);
          break;
        }
      }
    }
    if (cookie == null) {
      cookie = new Cookie(pName, pValue);
      cookie.setMaxAge(Integer.MAX_VALUE);
    }
    //application path is either root "/" of server address
    //or WEB application name e.g. /bsa-433
    String path = this.httpReq.getServletContext().getContextPath();
    if ("".equals(path)) {
      path = "/";
    }
    cookie.setPath(path);
    this.httpResp.addCookie(cookie);
  }

  /**
   * <p>Returns context path.</p>
   * @return context path
   **/
  @Override
  public final String getCtxPth() {
    return this.httpReq.getServletContext().getContextPath();
  }

  /**
   * <p>Returns preferred clients locale.</p>
   * @return locale
   **/
  @Override
  public final Locale getLocale() {
    return this.httpReq.getLocale();
  }

  /**
   * <p>Returns client's request URL without parameters.</p>
   * @return URL
   **/
  @Override
  public final StringBuffer getReqUrl() {
    return this.httpReq.getRequestURL();
  }

  /**
   * <p>Returns remote host.</p>
   * @return remote host
   **/
  @Override
  public final String getRemHost() {
    return this.httpReq.getRemoteHost();
  }

  /**
   * <p>Returns remote address.</p>
   * @return remote address
   **/
  @Override
  public final String getRemAddr() {
    return this.httpReq.getRemoteAddr();
  }

  /**
   * <p>Returns remote user.</p>
   * @return remote user
   **/
  @Override
  public final String getRemUsr() {
    return this.httpReq.getRemoteUser();
  }

  /**
   * <p>Returns remote port.</p>
   * @return remote port
   **/
  @Override
  public final int getRemPort() {
    return this.httpReq.getRemotePort();
  }

  //Simple getters and setters:
  /**
   * <p>Geter for httpReq.</p>
   * @return HttpServletRequest
   **/
  public final HttpServletRequest getHttpReq() {
    return this.httpReq;
  }

  /**
   * <p>Getter for httpResp.</p>
   * @return HttpServletResponse
   **/
  public final HttpServletResponse getHttpResp() {
    return this.httpResp;
  }
}
