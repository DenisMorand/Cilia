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

package fr.liglab.adele.cilia.model;

import java.util.Dictionary;
import java.util.Hashtable;



/**
 * This class is a mediator representation model.
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class Mediator extends MediatorComponent {
	/**
	 * 
	 * Creates a new mediator representation model.
	 * 
	 * @param id
	 *            identificator of the new mediator.
	 * @param type
	 *            type of the mediator representation model.
	 * @param nspace
	 *            classname of the mediator representation model.
	 * @param properties
	 *            new properties to add to the mediator representation model.
	 * @param chain
	 *            Chain where this mediator will be.
	 */

	public Mediator(String id, String type, String nspace, String catego,
			Dictionary properties, Chain chain) {
		super(id, type, nspace, catego, properties, chain);
		
	}

	/**
	 * Creates a new mediator representation model.
	 * 
	 * @param id
	 *            identificator of the new mediator.
	 * @param type
	 *            type of the mediator representation model.
	 * @param classname
	 *            classname of the mediator representation model.
	 * @param properties
	 *            new properties to add to the mediator representation model.
	 */
	public Mediator(String id, String type, String namespace, Dictionary properties) {
		super(id, type, namespace, null, properties, null);
	}

	/**
	 * 
	 * Creates a new mediator representation model.
	 * 
	 * @param id
	 *            identificator of the new mediator.
	 * @param type
	 *            type of the mediator representation model.
	 * @param properties
	 *            new properties to add to the mediator representation model.
	 */
	public Mediator(String id, String type, Dictionary properties) {
		super(id, type, null, null, properties, null);
	}

	/**
	 * 
	 * Creates a new mediator representation model.
	 * 
	 * @param id
	 *            identificator of the new mediator.
	 * @param type
	 *            type of the mediator representation model.
	 * @param properties
	 *            new properties to add to the mediator representation model.
	 * @param chain
	 *            Chain where this mediator will be.
	 */
	public Mediator(String id, String type, Dictionary properties, Chain chain) {
		super(id, type, null, null, properties, chain);
	}

	/**
	 * 
	 * Creates a new mediator representation model.
	 * 
	 * @param id
	 *            identificator of the new mediator.
	 * @param type
	 *            type of the mediator representation model.
	 * @param properties
	 *            new properties to add to the mediator representation model.
	 * @param chain
	 *            Chain where this mediator will be.
	 */
	public Mediator(String id, String type, Chain chain) {
		super(id, type, new Hashtable(), chain);
	}

	/**
	 * 
	 * Creates a new mediator representation model.
	 * 
	 * @param id
	 *            identificator of the new mediator.
	 * @param type
	 *            type of the mediator representation model.
	 */
	public Mediator(String id, String type) {
		super(id, type, new Hashtable(), null);
	}

	/**
	 * Set the chain representation model which will contain this mediator.
	 * 
	 * @param chain
	 *            chain which will contain this mediator.
	 */
	public void setChain(Chain chain) {
		synchronized (lockObject) {
			if (chain != null) {
				//chain.add(this);
				setQualifiedId(Component.buildQualifiedId(getId(),chain.getId()));
			}
			super.chain = chain ;
		}
	}
	
}
