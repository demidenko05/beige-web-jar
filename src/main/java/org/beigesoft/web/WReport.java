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
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.beigesoft.exc.ExcCode;
import org.beigesoft.fct.IFctApp;
import org.beigesoft.log.ILog;
import org.beigesoft.hnd.IHndRq;
import org.beigesoft.hnd.IHndFlRpRq;
import org.beigesoft.rdb.IOrm;

/**
 * <p>Generic servlet that passes response output stream to file reporter
 * request handler. It's for PDF, CSV, etc. responces.</p>
 * @author Yury Demidenko
 */
@SuppressWarnings("serial")
public class WReport extends HttpServlet {

  /**
   * <p>App beans factory.</p>
   **/
  private IFctApp fctApp;

  /**
   * <p>Logger name.</p>
   **/
  private String logNm;

  /**
   * <p>Content type, e.g. "text/csv".</p>
   **/
  private String contTy;

  /**
   * <p>File extention, e.g. "csv".</p>
   **/
  private String fileEx;

  /**
   * <p>File reporter handler name.</p>
   **/
  private String hndNm;

  /**
   * <p>Request basic first handlers names.</p>
   **/
  private final List<String> hndNms = new ArrayList<String>();

  @Override
  public final void init() throws ServletException {
    this.hndNm = getInitParameter("hndNm");
    this.logNm = getInitParameter("logNm");
    this.contTy = getInitParameter("contTy");
    this.fileEx = getInitParameter("fileEx");
    String hndNmsStr = getInitParameter("hndNms");
    if (hndNmsStr != null) {
      for (String hn : hndNmsStr.split(",")) {
        this.hndNms.add(hn);
      }
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
    pResp.setContentType(this.contTy);
    String fileNm = pReq.getParameter("fileNm");
    if (fileNm == null || "".equals(fileNm)) {
      fileNm = "data";
    }
    if (this.fileEx != null) { //to preview (prevent downloading) leave it null
      pResp.setHeader("Content-Disposition", "attachment; filename="
        + fileNm + "." + this.fileEx);
    }
    OutputStream htmOus = null;
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
      htmOus = pResp.getOutputStream();
      IHndFlRpRq hnd = (IHndFlRpRq) this.fctApp.laz(rqVs, this.hndNm);
      hnd.handle(rqVs, rqDt, htmOus);
      //any handler can set redirect servlet, e.g. first handler spam redir:
      String srvlRd = (String) rqDt.getAttr("srvlRd");
      if (srvlRd != null) {
        rqDt.remAttr("srvlRd");
        pResp.sendRedirect(pReq.getContextPath() + srvlRd);
      }
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
    } finally {
      if (htmOus != null) {
        htmOus.close();
      }
    }
  }
}
