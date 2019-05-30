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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

import org.beigesoft.fct.IFctApp;
import org.beigesoft.fct.IFctAsm;
import org.beigesoft.fct.IIniBdFct;

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
    String iniBdFctCls = pCnt.getInitParameter("iniBdFctCls");
    Object inifc = Class.forName(iniBdFctCls).newInstance();
    @SuppressWarnings("unchecked")
    IIniBdFct<RS> inif = (IIniBdFct<RS>) inifc;
    inif.iniBd(pRvs, fct, new Ctx(pCnt));
    fct.init(pRvs, new CtxAttr(pCnt));
    //session tracker:
    SesTrk st = new SesTrk();
    st.setLog(fct.getFctBlc().lazLogSec(pRvs));
    st.setI18n(fct.getFctBlc().lazI18n(pRvs));
    pCnt.setAttribute("sesTrk", st);
    pCnt.setAttribute("i18n", st.getI18n());
    pCnt.setAttribute("IFctApp", fct);
  }
}
