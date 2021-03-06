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

package fr.liglab.adele.cilia.runtime.impl;

import fr.liglab.adele.cilia.BindingReadOnly;
import fr.liglab.adele.cilia.ChainReadOnly;
import fr.liglab.adele.cilia.MediatorReadOnly;
import fr.liglab.adele.cilia.PortReadOnly;
import fr.liglab.adele.cilia.model.Binding;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.Mediator;
import fr.liglab.adele.cilia.model.Port;
import fr.liglab.adele.cilia.model.Sender;

public class BindingReadOnlyImpl implements BindingReadOnly {
	final Binding binding;


	public BindingReadOnlyImpl(Binding binding) {
		this.binding = binding;
	}

	public String getId() {
		return binding.getId();
	}

	public String getType() {
		return binding.getType();
	}

	public String getNamespace() {
		return binding.getNamespace();
	}

	public ChainReadOnly getChain() {
		return new ChainReadOnlyImpl(binding.getChain());
	}

	public PortReadOnly getSourcePort() {
		return new PortReadOnlyImpl(binding.getSourcePort()) ;
	}

	public PortReadOnly getTargetPort() {
		return new PortReadOnlyImpl(binding.getTargetPort()) ;
	}

	public MediatorReadOnly getSourceMediator() {
		return new MediatorReadOnlyImpl(binding.getSourceMediator());
	}

	public MediatorReadOnly getTargetMediator() {
		return new MediatorReadOnlyImpl(binding.getTargetMediator());
	}

	public String getSenderId() {
		return binding.getSender().getId();
	}

	public String getSenderType() {
		return binding.getSender().getType();
	}

	public String getSenderNameSpace() {
		return binding.getSender().getNamespace();
	}

}
