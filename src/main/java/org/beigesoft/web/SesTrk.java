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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.beigesoft.log.ILog;
import org.beigesoft.srv.II18n;

/**
 * <p>Service that tracks sessions.</p>
 * @author Yury Demidenko
 */
public class SesTrk implements ISesTrk {

  /**
   * <p>Log.</p>
   **/
  private ILog log;

  /**
   * <p>I18N service.</p>
   **/
  private II18n i18n;

  /**
   * <p>Tracks new session if need.</p>
   * @param pReq HTTP request
   * @throws Exception - an exception
   **/
  @Override
  public final void track(final HttpServletRequest pReq) throws Exception {
    HttpSession session = pReq.getSession();
    if (session.getAttribute("newSesTrkd") == null) {
      // scamware can make any headers, so user-agent is not informative
      String msg = "New session from IP/port/host: " + pReq.getRemoteAddr()
        + "/" + pReq.getRemotePort() + "/" + pReq.getRemoteHost();
      this.log.info(null, getClass(), msg);
      session.setAttribute("newSesTrkd", Boolean.TRUE);
    }
  }

  /**
   * <p>Tracks login fail.</p>
   * @param pReq HTTP request
   * @throws Exception - an exception
   **/
  @Override
  public final void fail(final HttpServletRequest pReq) throws Exception {
    HttpSession session = pReq.getSession();
    Integer flCntAtmp = (Integer) session.getAttribute("flCntAtmp");
    if (flCntAtmp == null) {
      flCntAtmp = 0;
    }
    flCntAtmp++;
    session.setAttribute("flCntAtmp", flCntAtmp);
    if (flCntAtmp < 6) {
      pReq.setAttribute("loginErrorJsp",
        i18n.getMsg("invalid_user_name_or_password"));
      // scamware can make any headers, so user-agent is not informative
      String msg = "Fail login attempt from IP/port/host/attempt: "
        + pReq.getRemoteAddr() + "/" + pReq.getRemotePort()
          + "/" + pReq.getRemoteHost() + "/" + flCntAtmp;
      log.warn(null, getClass(), msg);
    } else {
      // banned for web.xml session-timeout minits
      session.setAttribute("isBanned", Boolean.TRUE);
      pReq.setAttribute("loginBanJsp", this.i18n.getMsg("loginBan"));
      String msg = "Ban login attempt from IP/port/host/attempt: "
        + pReq.getRemoteAddr() + "/" + pReq.getRemotePort()
          + "/" + pReq.getRemoteHost() + "/" + flCntAtmp;
      log.error(null, getClass(), msg);
    }
  }

  //SGS:
  /**
   * <p>Getter for log.</p>
   * @return ILog
   **/
  public final ILog getLog() {
    return this.log;
  }

  /**
   * <p>Setter for log.</p>
   * @param pLog reference
   **/
  public final void setLog(final ILog pLog) {
    this.log = pLog;
  }

  /**
   * <p>Getter for i18n.</p>
   * @return II18n
   **/
  public final II18n getI18n() {
    return this.i18n;
  }

  /**
   * <p>Setter for i18n.</p>
   * @param pI18n reference
   **/
  public final void setI18n(final II18n pI18n) {
    this.i18n = pI18n;
  }
}
