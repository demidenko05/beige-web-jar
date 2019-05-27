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

import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.io.File;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;

import org.beigesoft.log.ILog;
import org.beigesoft.mdlp.EmMsg;
import org.beigesoft.mdlp.EmAtch;
import org.beigesoft.mdlp.EmInt;
import org.beigesoft.mdlp.EmStr;
import org.beigesoft.srv.IEmSnd;

/**
 * <p>Service that sends email with Java-mail API.</p>
 *
 * @author Yury Demidenko
 */
public class Mailer implements IEmSnd {

  /**
   * <p>Log.</p>
   **/
  private ILog log;

  /**
   * <p>WEB-app path for already uploaded files
   * in WEB-application.</p>
   **/
  private String appPth;

  /**
   * <p>Only constructor.</p>
   * @param pLog log
   **/
  public Mailer(final ILog pLog) {
    this.log = pLog;
    MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
    boolean mcapOk = false;
    for (String mcap : mc.getMimeTypes()) {
      if (mcap.contains("multipart")) {
        mcapOk = true;
        break;
      }
    }
    if (!mcapOk) { //Fix android
      mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
      mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
      mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
      mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
      mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");
      this.log.info(null, Mailer.class, "Mailcap has been fixed");
    }
  }

  /**
   * <p>Sends email.</p>
   * @param pRvs request scoped vars
   * @param pMsg msg to mail
   * @throws Exception - an exception
   **/
  @Override
  public final void send(final Map<String, Object> pRvs,
    final EmMsg pMsg) throws Exception {
    Properties props = new Properties();
    for (EmStr esp : pMsg.getEmCn().getStrPrps()) {
      props.put(esp.getPrNm(), esp.getPrVl());
    }
    for (EmInt eip : pMsg.getEmCn().getIntPrps()) {
      props.put(eip.getPrNm(), eip.getPrVl());
    }
    Session sess = Session.getInstance(props);
    Message msg = new MimeMessage(sess);
    msg.setFrom(new InternetAddress(pMsg.getEmCn().getEml()));
    if (pMsg.getRcps().size() == 1) {
      msg.setRecipient(Message.RecipientType.TO,
        new InternetAddress(pMsg.getRcps().get(0).getEml().getIid()));
    } else {
      InternetAddress[] adrs =
        new InternetAddress[pMsg.getRcps().size()];
      for (int i = 0; i < pMsg.getRcps().size(); i++) {
        adrs[i] = new InternetAddress(pMsg.getRcps().get(i).getEml().getIid());
      }
      msg.setRecipients(Message.RecipientType.TO, adrs);
    }
    msg.setSubject(pMsg.getSubj());
    if (pMsg.getAtchs().size() > 0) {
      MimeBodyPart mbpt = new MimeBodyPart();
      mbpt.setText(pMsg.getTxt());
      Multipart mp = new MimeMultipart();
      mp.addBodyPart(mbpt);
      for (EmAtch attch : pMsg.getAtchs()) {
        File fl = new File(attch.getPth());
        if (!fl.exists()) {
          //URI uploaded relative to WEB-APP path:
          String fp = attch.getPth();
          if (!"/".equals(File.separator)) {
            fp = fp.replace("/", "\\");
          }
          fl = new File(this.appPth + File.separator + fp);
        }
        if (fl.exists()) {
          MimeBodyPart mbp = new MimeBodyPart();
          mbp.attachFile(fl);
          mp.addBodyPart(mbp);
        } else {
          throw new Exception("There is no file: " + attch.getPth());
        }
      }
      msg.setContent(mp);
    } else {
      msg.setText(pMsg.getTxt());
    }
    msg.setSentDate(new Date());
    Transport.send(msg, pMsg.getEmCn().getEml(), pMsg.getEmCn().getPwd());
  }

  //Simple getters and setters:
  /**
   * <p>Geter for log.</p>
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
   * <p>Getter for appPth.</p>
   * @return String
   **/
  public final String getAppPth() {
    return this.appPth;
  }

  /**
   * <p>Setter for appPth.</p>
   * @param pAppPth reference
   **/
  public final void setAppPth(final String pAppPth) {
    this.appPth = pAppPth;
  }
}
