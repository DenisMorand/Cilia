/*
 * Copyright Adele Team LIG
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.liglab.adele.cilia.administration.processors;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.utils.AdminData;
import fr.liglab.adele.cilia.framework.utils.Const;
import fr.liglab.adele.cilia.model.Adapter;
import fr.liglab.adele.cilia.model.Binding;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.Mediator;

public class CiliaReplacerProcessor {
	private static final Logger logger = LoggerFactory.getLogger(Const.LOGGER_ADAPTATION);

	/**
	 * The Cilia context.
	 */
	CiliaContext ccontext;

	BundleContext m_bundleContext;

	public CiliaReplacerProcessor(BundleContext bc) {
		m_bundleContext = bc;
	}

	private ServiceReference retreiveAdminService(String chainId) {
		ServiceReference[] refs = null;
		ServiceReference refData;
		BundleContext context = m_bundleContext;
		try {
			refs = context.getServiceReferences(AdminData.class.getName(), "(chain.name="
					+ chainId + ")");
		} catch (InvalidSyntaxException e) {
			logger.error("Admin data service lookup unrecoverable error");
			throw new RuntimeException("Admin data service lookup unrecoverable error");
		}
		if (refs != null)
			refData = refs[0];
		else {
			refData = null;
		}
		return refData;
	}

	private void copyData(String chainId, String mediatorSource, String mediatorDest) {
		AdminData dataContainer;
		ServiceReference refData;

		refData = retreiveAdminService(chainId);
		if (refData != null) {
			dataContainer = (AdminData) m_bundleContext.getService(refData);
			dataContainer.copyData(mediatorSource, mediatorDest);
			m_bundleContext.ungetService(refData);
		}

	}

	/**
	 * The main process method, this method is called by the cilia framework.
	 * 
	 * @param data
	 *            contains the parameters to replace a cilia chain element
	 *            instance.
	 * @return the same unchanged data.
	 */
	protected Data replace(Data data) {
		try {
			ccontext.getMutex().writeLock().acquire();
		} catch (InterruptedException e) {
		}
		try {
			if ("mediator"
					.compareToIgnoreCase(String.valueOf(data.getProperty("element"))) == 0) {
				replaceMediator(data);
			} else if ("adapter".compareToIgnoreCase(String.valueOf(data
					.getProperty("element"))) == 0) {
				replaceAdapter(data);
			}
		} finally {
			ccontext.getMutex().writeLock().release();
		}
		return data;
	}

	private void replaceMediator(Data data) {
		Chain chain;
		Binding[] bindings;
		Mediator mediatorSource, mediatorDest;
		String chainId = String.valueOf(data.getProperty("chain"));
		String mediatorIdSource = String.valueOf(data.getProperty("id"));
		String mediatorIdDest = String.valueOf(data.getProperty("by"));
		if ((chainId == null) || (mediatorIdSource == null) || (mediatorIdDest == null)) {
			logger.error("Missing parameter(s)");
			return;
		}
		chain = ccontext.getChain(chainId);
		if (chain == null) {
			logger.error("Chain [{}] not found." + chainId);
			return;
		}
		mediatorSource = ccontext.getChain(chainId).getMediator(mediatorIdSource);
		if (mediatorSource == null) {
			logger.error("Mediator [{}] not found.", mediatorIdSource);
			return;
		}
		mediatorDest = ccontext.getChain(chainId).getMediator(mediatorIdDest);

		if (mediatorDest == null) {
			logger.error("Mediator [{}] not found.", mediatorIdDest);
			return;
		}
		logger.info("Command 'replace mediator' [{}] by [{}]",
				mediatorSource.getQualifiedId(), mediatorDest.getQualifiedId());

		bindings = mediatorSource.getInBindings();

		mediatorSource.lockRuntime();
		mediatorDest.lockRuntime();
		if (bindings != null) {
			for (int i = 0; i < bindings.length; i++) {
				chain.bind(bindings[i].getSourcePort(),
						mediatorDest.getInPort(bindings[i].getTargetPort().getName()));
				chain.unbind(bindings[i]);
			}

		}
		bindings = mediatorSource.getOutBindings();
		if (bindings != null) {
			for (int i = 0; i < bindings.length; i++) {
				chain.bind(mediatorDest.getInPort(bindings[i].getSourcePort().getName()),
						bindings[i].getTargetPort());
				chain.unbind(bindings[i]);
			}
		}
		/* Now data are injected */
		copyData(chain.getId(), mediatorSource.getId(), mediatorDest.getId());
		mediatorDest.unLockRuntime();
		mediatorSource.unLockRuntime();
	}

	private void replaceAdapter(Data data) {
		Chain chain;
		Binding[] bindings;
		Adapter adapter, adapterNew;
		String chainId = String.valueOf(data.getProperty("chain"));
		String adapterSource = String.valueOf(data.getProperty("id"));
		String adapterDest = String.valueOf(data.getProperty("by"));
		if ((chainId == null) || (adapterSource == null) || (adapterDest == null)) {
			logger.error("Missing parameter(s)");
			return;
		}
		chain = ccontext.getChain(chainId);
		if (chain == null) {
			logger.error("Chain [{}] not found." + chainId);
			return;
		}
		adapter = ccontext.getChain(chainId).getAdapter(adapterSource);
		if (adapter == null) {
			logger.error("Adapter [{}] not found.", adapterSource);
			return;
		}
		adapterNew = ccontext.getChain(chainId).getAdapter(adapterDest);

		if (adapterNew == null) {
			logger.error("Adapter [{}] not found.", adapterSource);
			return;
		}

		logger.info("Command 'replace adapter' [{}] by [{}]",
				adapter.getQualifiedId(), adapterNew.getQualifiedId());

		bindings = adapter.getInBindings();

		adapter.lockRuntime();
		adapterNew.lockRuntime();
		if (bindings != null) {
			for (int i = 0; i < bindings.length; i++) {
				chain.bind(bindings[i].getSourcePort(),
						adapterNew.getInPort(bindings[i].getTargetPort().getName()));
			}

		}
		bindings = adapter.getOutBindings();
		if (bindings != null) {
			for (int i = 0; i < bindings.length; i++) {
				chain.bind(adapterNew.getInPort(bindings[i].getSourcePort().getName()),
						bindings[i].getTargetPort());
			}
		}
		/* Remove previous bindings */
		bindings = adapter.getInBindings();

		if (bindings != null) {
			for (int i = 0; i < bindings.length; i++) {
				chain.unbind(bindings[i]);
			}
		}

		bindings = adapter.getOutBindings();
		if (bindings != null) {
			for (int i = 0; i < bindings.length; i++) {
				chain.unbind(bindings[i]);
			}
		}
		copyData(chain.getId(), adapter.getId(), adapterNew.getId());
		adapterNew.unLockRuntime();
		adapter.unLockRuntime();
	}
}
