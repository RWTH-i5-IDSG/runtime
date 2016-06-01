/*
 * Copyright (c) 2012, intarsys consulting GmbH
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of intarsys nor the names of its contributors may be used
 *   to endorse or promote products derived from this software without specific
 *   prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package de.intarsys.claptz.impl;

import de.intarsys.claptz.IExtension;
import de.intarsys.tools.component.ConfigurationException;
import de.intarsys.tools.exception.TunnelingException;
import de.intarsys.tools.infoset.ElementTools;
import de.intarsys.tools.infoset.IElement;
import de.intarsys.tools.infoset.IElementConfigurable;
import de.intarsys.tools.proxy.IProxy;
import de.intarsys.tools.reflect.ObjectCreationException;

/**
 * A generic implementation to ease implementation of "deferred" objects
 * declared via an {@link IExtension}.
 * <p>
 * This object encapsulates the {@link IExtension} and the {@link Element},
 * preparing for realization of the intended object on demand. Two common
 * scenarios are supported. In the first, the provider of the extension point
 * itself creates the {@link ExtensionObjectProxy} directly to avoid the cost of
 * reflective class access. In the second, a concrete factory object may be
 * derived from {@link ExtensionObjectProxy} to inherit its lazyness with regard
 * to hosting an object to be realized. The concrete proxy class name may be
 * declared in an another element attribute than "class".
 * 
 */
public class ExtensionObjectProxy implements IElementConfigurable, IProxy {

	/**
	 * The link to the definition element in the extension
	 */
	private IElement element;

	private Object realized;

	final private Class proxyClass;

	final private String proxyClassAttribute;

	final private Object context;

	public ExtensionObjectProxy(Class pProxyClass, Object context,
			IElement pElement) {
		this.context = context;
		element = pElement;
		proxyClass = pProxyClass;
		proxyClassAttribute = "class";
	}

	public ExtensionObjectProxy(Class pProxyClass, Object context,
			IElement pElement, String classAttribute) {
		this.context = context;
		element = pElement;
		proxyClass = pProxyClass;
		proxyClassAttribute = classAttribute;
	}

	protected Object basicGetRealized() {
		return realized;
	}

	@Override
	public void configure(IElement element)
			throws ConfigurationException {
		this.element = element;
	}

	public Object getContext() {
		return context;
	}

	public IElement getElement() {
		return element;
	}

	public Class getProxyClass() {
		return proxyClass;
	}

	public String getProxyClassAttribute() {
		return proxyClassAttribute;
	}

	@Override
	synchronized public Object getRealized() {
		if (realized == null) {
			try {
				realized = realize();
			} catch (ObjectCreationException e) {
				throw new TunnelingException(e);
			}
		}
		return realized;
	}

	protected Object realize() throws ObjectCreationException {
		Object object = ElementTools.createObject(getElement(),
				getProxyClassAttribute(), getProxyClass(), getContext());
		return object;
	}
}
