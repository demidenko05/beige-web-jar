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

import javax.servlet.ServletContext;

import org.beigesoft.hld.IAttrs;

/**
 * <p>Wrapper (adapter) of HttpServlet servlet context attributes.</p>
 *
 * @author Yury Demidenko
 */
public class CtxAttr implements IAttrs {

  /**
   * <p>Http Servlet Context to adapt.</p>
   **/
  private final ServletContext svrlCtx;

  /**
   * <p>Only constructor.</p>
   * @param pSvrlCtx reference
   **/
  public CtxAttr(final ServletContext pSvrlCtx) {
    this.svrlCtx = pSvrlCtx;
  }

  /**
   * <p>Getter for attribute.</p>
   * @param pAttrName Attribute name
   * @return Attribute
   **/
  @Override
  public final Object getAttr(final String pAttrName) {
    return this.svrlCtx.getAttribute(pAttrName);
  }

  /**
   * <p>Setter for attribute.</p>
   * @param pAttrName Attribute name
   * @param pAttribute reference
   **/
  @Override
  public final void setAttr(final String pAttrName,
    final Object pAttribute) {
    this.svrlCtx.setAttribute(pAttrName, pAttribute);
  }

  /**
   * <p>Removes attribute.</p>
   * @param pAttrName Attribute name
   **/
  @Override
  public final void remAttr(final String pAttrName) {
    this.svrlCtx.removeAttribute(pAttrName);
  }
}
