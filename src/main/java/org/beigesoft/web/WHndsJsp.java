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

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;

import org.beigesoft.exc.ExcCode;
import org.beigesoft.fct.IFctApp;
import org.beigesoft.log.ILog;
import org.beigesoft.hnd.IHndRq;
import org.beigesoft.rdb.IOrm;

/**
 * <p>Generic servlet that delegates request to chain of request handlers
 * that can implement any business logic e.g. report, form edit,
 * entities list, etc. Finally it invokes JSP renderer: default JSP
 * (by web.xml), it maybe overridden by invoker JSP name or by any handler.
 * Any handler may also requests servlet redirection.
 * WEB-CRUD uses AJAX-HTML5-FormData that makes multi-part POST request,
 * so it should be configured with "multipart-config".</p>
 *
 * @author Yury Demidenko
 */
@SuppressWarnings("serial")
public class WHndsJsp extends HttpServlet {

  /**
   * <p>App beans factory.</p>
   **/
  private IFctApp fctApp;

  /**
   * <p>Folder for redirected JSP, e.g. "JSP WEB-INF/jsp/".
   * Settled through init params.</p>
   **/
  private String dirJsp;

  /**
   * <p>Logger name.</p>
   **/
  private String logNm;

  /**
   * <p>Request handlers names.</p>
   **/
  private final List<String> hndNms = new ArrayList<String>();

  /**
   * <p>Default JSP name without extension (.jsp).</p>
   **/
  private String defJsp;

  @Override
  public final void init() throws ServletException {
    this.dirJsp = getInitParameter("dirJsp");
    this.defJsp = getInitParameter("defJsp");
    this.logNm = getInitParameter("logNm");
    String hndNmsStr = getInitParameter("hndNms");
    for (String hn : hndNmsStr.split(",")) {
      this.hndNms.add(hn);
    }
    this.fctApp = (IFctApp) getServletContext().getAttribute("IFctApp");
  }

  @Override
  public final void doGet(final HttpServletRequest pReq,
    final HttpServletResponse pResp) throws ServletException, IOException {
    doWork(pReq, pResp);
  }

  @Override
  public final void doPost(final HttpServletRequest pReq,
    final HttpServletResponse pResp) throws ServletException, IOException {
    doWork(pReq, pResp);
  }


  /**
   * <p>Generic request handler.</p>
   * @param pReq Http Servlet Request
   * @param pResp Http Servlet Response
   * @throws ServletException ServletException
   * @throws IOException IOException
   **/
  public final void doWork(final HttpServletRequest pReq,
    final HttpServletResponse pResp) throws ServletException, IOException {
    HashMap<String, Object> rqVs = new HashMap<String, Object>();
    pReq.setCharacterEncoding("UTF-8");
    pResp.setCharacterEncoding("UTF-8");
    try {
      HttpReqDt rqDt = new HttpReqDt(pReq, pResp);
      rqDt.setAttr("rvs", rqVs);
      rqVs.put("rqDt", rqDt);
      for (String hn : this.hndNms) {
        IHndRq hnd = (IHndRq) this.fctApp.laz(rqVs, hn);
        hnd.handle(rqVs, rqDt);
        //any handler can set redirect servlet, e.g. first handler spam redir:
        String srvlRd = (String) rqDt.getAttr("srvlRd");
        if (srvlRd != null) {
          rqDt.remAttr("srvlRd");
          pResp.sendRedirect(pReq.getContextPath() + srvlRd);
        }
      }
      String rnd = (String) rqDt.getAttr("rnd");
      if (rnd == null) {
        rnd = pReq.getParameter("rnd");
      }
      if (rnd == null) {
        rnd = this.defJsp;
      }
      String path = dirJsp + rnd + ".jsp";
      RequestDispatcher rd = getServletContext().getRequestDispatcher(path);
      rd.include(pReq, pResp);
    } catch (Exception e) {
      if (this.fctApp != null) {
        ILog logger = null;
        try {
          logger = (ILog) this.fctApp.laz(rqVs, this.logNm);
          logger.error(rqVs, getClass(), "WORK", e);
        } catch (Exception e1) {
          e1.printStackTrace();
          e.printStackTrace();
        }
      } else {
        e.printStackTrace();
      }
      if (e instanceof ExcCode) {
        ExcCode ec = (ExcCode) e;
        if (ec.getCode() == ExcCode.WRPR || ec.getCode() == ExcCode.BUSY
          || ec.getCode() == IOrm.DRTREAD) {
          pReq.setAttribute("error_code", ec.getCode());
          pReq.setAttribute("short_message", ec.getShMsg());
        } else {
          pReq.setAttribute("error_code",
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
      } else {
        pReq.setAttribute("error_code",
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      pReq.setAttribute("javax.servlet.error.status_code",
        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      pReq.setAttribute("javax.servlet.error.exception", e);
      pReq.setAttribute("javax.servlet.error.request_uri",
        pReq.getRequestURI());
      pReq.setAttribute("javax.servlet.error.servlet_name", getClass()
        .getCanonicalName());
      pResp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
