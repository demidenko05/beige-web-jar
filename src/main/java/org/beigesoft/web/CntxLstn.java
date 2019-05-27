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

import java.util.Map;
import java.util.HashMap;
import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

import org.beigesoft.fct.IFctApp;
import org.beigesoft.fct.IFctAsm;
import org.beigesoft.fct.IIniBdFct;
import org.beigesoft.prp.ISetng;
import org.beigesoft.rdb.IOrm;

/**
 * <p>Initializes and releases application.</p>
 *
 * @param <RS> platform dependent record set type
 * @author Yury Demidenko
 */
public class CntxLstn<RS> implements ServletContextListener {

  /**
   * <p>Initializes main factory.</p>
   **/
  @Override
  public final void contextInitialized(final ServletContextEvent sce) {
    HashMap<String, Object> rvs = new HashMap<String, Object>();
    try {
      make(rvs, sce.getServletContext());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * <p>Invokes main factory releasing (including closing DS).</p>
   **/
  @Override
  public final void contextDestroyed(final ServletContextEvent sce) {
    try {
      IFctApp fct = (IFctApp) sce.getServletContext().getAttribute("IFctApp");
      if (fct != null) {
        HashMap<String, Object> rvs = new HashMap<String, Object>();
        fct.release(rvs);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * <p>Initializes factory.</p>
   * @param pRvs request scoped vars
   * @param pCnt factory and servlet
   * @throws Exception - an exception
   **/
  public final void make(final Map<String, Object> pRvs,
    final ServletContext pCnt) throws Exception {
    String fctAppCls = pCnt.getInitParameter("fctAppCls");
    Object fctc = Class.forName(fctAppCls).newInstance();
    @SuppressWarnings("unchecked")
    IFctAsm<RS> fct = (IFctAsm<RS>) fctc;
    makeVars(pRvs, pCnt, fct);
    String iniBdFctCls = pCnt.getInitParameter("iniBdFctCls");
    Object inifc = Class.forName(iniBdFctCls).newInstance();
    @SuppressWarnings("unchecked")
    IIniBdFct<RS> inif = (IIniBdFct<RS>) inifc;
    inif.iniBd(pRvs, fct);
    fct.init(pRvs, new CtxAttr(pCnt));
    //session tracker:
    SesTrk st = new SesTrk();
    st.setLog(fct.getFctBlc().lazLogSec(pRvs));
    st.setI18n(fct.getFctBlc().lazI18n(pRvs));
    pCnt.setAttribute("sesTrk", st);
    pCnt.setAttribute("i18n", st.getI18n());
    pCnt.setAttribute("IFctApp", fct);
  }

  /**
   * <p>Makes variables from web.xml.</p>
   * @param pRvs request scoped vars
   * @param pCnt factory and servlet
   * @param pFct factory app
   * @throws Exception - an exception
   **/
  public final void makeVars(final Map<String, Object> pRvs,
    final ServletContext pCnt, final IFctAsm<RS> pFct) throws Exception {
    pFct.getFctBlc().getFctDt().setUplDir(pCnt.getInitParameter("uplDir"));
    pFct.getFctBlc().getFctDt().setStgUvdDir(pCnt.getInitParameter("uvdDir"));
    pFct.getFctBlc().getFctDt().setStgOrmDir(pCnt.getInitParameter("ormDir"));
    pFct.getFctBlc().getFctDt().setStgDbCpDir(pCnt.getInitParameter("dbcpDir"));
    pFct.getFctBlc().getFctDt().setLngCntr(pCnt.getInitParameter("lngCntr"));
    pFct.getFctBlc().getFctDt().setNewDbId(Integer.parseInt(pCnt
      .getInitParameter("newDbId")));
    pFct.getFctBlc().getFctDt().setDbgSh(Boolean.parseBoolean(pCnt
      .getInitParameter("dbgSh")));
    pFct.getFctBlc().getFctDt().setDbgFl(Integer.parseInt(pCnt
      .getInitParameter("dbgFl")));
    pFct.getFctBlc().getFctDt().setDbgCl(Integer.parseInt(pCnt
      .getInitParameter("dbgCl")));
    pFct.getFctBlc().getFctDt().setWriteTi(Integer.valueOf(pCnt
      .getInitParameter("writeTi")));
    pFct.getFctBlc().getFctDt().setReadTi(Integer.valueOf(pCnt
      .getInitParameter("readTi")));
    pFct.getFctBlc().getFctDt().setWriteReTi(Integer.valueOf(pCnt
      .getInitParameter("writeReTi")));
    pFct.getFctBlc().getFctDt().setWrReSpTr(Boolean.valueOf(pCnt
      .getInitParameter("wrReSpTr")));
    pFct.getFctBlc().getFctDt().setLogSize(Integer.parseInt(pCnt
      .getInitParameter("logSize")));
    File appPth = new File(pCnt.getRealPath(""));
    pFct.getFctBlc().getFctDt().setAppPth(appPth.getPath());
    pFct.getFctBlc().getFctDt()
      .setLogPth(pFct.getFctBlc().getFctDt().getAppPth());
    ISetng setng = pFct.getFctBlc().lazStgOrm(pRvs);
    String dbUrl = setng.lazCmnst().get(IOrm.DBURL);
    if (dbUrl.contains(IOrm.CURDIR)) { //sqlite
      dbUrl = dbUrl.replace(IOrm.CURDIR, pFct.getFctBlc().getFctDt().getAppPth()
        + File.separator);
    }
    pFct.getFctBlc().getFctDt().setDbUrl(dbUrl);
    String dbCls = setng.lazCmnst().get(IOrm.JDBCCLS);
    if (dbCls == null) {
      dbCls = setng.lazCmnst().get(IOrm.DSCLS);
    }
    pFct.getFctBlc().getFctDt().setDbCls(dbCls);
    pFct.getFctBlc().getFctDt().setDbUsr(setng.lazCmnst().get(IOrm.DBUSR));
    pFct.getFctBlc().getFctDt().setDbPwd(setng.lazCmnst().get(IOrm.DBPSW));
  }
}
