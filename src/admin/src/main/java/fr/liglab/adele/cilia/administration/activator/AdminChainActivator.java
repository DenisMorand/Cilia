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
package fr.liglab.adele.cilia.administration.activator;


import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.components.model.ContentBasedRouting;
import fr.liglab.adele.cilia.model.Adapter;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.Mediator;
import fr.liglab.adele.cilia.model.PatternType;
/**
 * AdminChainActivator: Generate and initialize the cilia-admin chan. 
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class AdminChainActivator {
	/**
	 * The cilia context, to add a mediation chain and initialize.
	 */
	CiliaContext ccontext;
	/**
	 * The cilia-admin chain.
	 */
	Chain adminChain;
	/**
	 * Method called when initializing the component.
	 * When the component is started, the cilia-admin chain is generated and initialized.
	 */
	protected void start() {
		adminChain = createChain();
		ccontext.addChain(adminChain);
		ccontext.startChain(adminChain);
	}
	/**
	 * when the component is stopped, the chain must be removed.
	 */
	protected void stop() {
		ccontext.removeChain(adminChain.getId());
	}
	/**
	 * Create the cilia-admin chain and its mediators.
	 * @return the cilia-admin chain reference.
	 */
	private Chain createChain(){
		Chain chain = new Chain("admin-chain", "AdministrationChain", "fr.liglab.adele.cilia", null);
		Adapter adapter = getAdapter();
		Mediator entry = getEntryMediator();
		Mediator creator = getCreatorMediator();
		Mediator shower =  getShowMediator();
		Mediator remover = getRemoverMediator();
		Mediator starter = getStarterMediator();
		Mediator stop =    getStopMediator();
		Mediator modifier =    getModifierMediator();
		Mediator loader =    getLoaderMediator();
		Mediator replacer = getReplacerMediator() ;
		Mediator copier = getCopierMediator();
		
		chain.add(adapter);
		chain.add(entry);
		chain.add(creator);
		chain.add(shower);
		chain.add(remover);
		chain.add(starter);
		chain.add(stop);
		chain.add(modifier);
		chain.add(loader);
		chain.add(replacer) ;
		chain.add(copier) ;
		
		chain.bind(adapter.getOutPort("adpsrv"), entry.getInPort("std"));
		chain.bind(entry.getOutPort("create"),creator.getInPort("creatorIn"));
		chain.bind(entry.getOutPort("remove"),remover.getInPort("removerIn"));
		chain.bind(entry.getOutPort("start"),starter.getInPort("starterIn"));
		chain.bind(entry.getOutPort("replace"),replacer.getInPort("replacerIn"));
		chain.bind(entry.getOutPort("copy"),copier.getInPort("copierIn"));
		chain.bind(entry.getOutPort("stop"),stop.getInPort("stopIn"));
		chain.bind(entry.getOutPort("show"),shower.getInPort("showIn"));
		chain.bind(entry.getOutPort("modify"),modifier.getInPort("modifyIn"));
		chain.bind(entry.getOutPort("load"),loader.getInPort("loaderIn"));
		chain.bind(entry.getOutPort("unload"),loader.getInPort("unloaderIn"));
		return chain;
	}

	/**
	 * @return
	 */
	private Adapter getAdapter() {
		// TODO Auto-generated method stub
		return new Adapter("service-adapter", "cilia-admin-service-adapter", "fr.liglab.adele.cilia", null, PatternType.IN_ONLY);
	}
	private Mediator getEntryMediator() {
		Mediator creator = new Mediator("admin-entry-mediator", "CiliaAdminCBMediator","fr.liglab.adele.cilia.admin", null);
		ContentBasedRouting routing = new ContentBasedRouting(creator);
		routing.evaluator("ldap");
		routing.condition("(data.content=create)").to("create");
		routing.condition("(data.content=remove)").to("remove");
		routing.condition("(data.content=start)").to("start");
		routing.condition("(data.content=stop)").to("stop");
		routing.condition("(data.content=show)").to("show");
		routing.condition("(data.content=modify)").to("modify");
		routing.condition("(data.content=load)").to("load");
		routing.condition("(data.content=unload)").to("unload");
		routing.condition("(data.content=replace)").to("replace");
		routing.condition("(data.content=copy)").to("copy");
		return creator;
	}
	
	private Mediator getCreatorMediator(){
		return new Mediator("admin-creator", "CiliaAdminCreator","fr.liglab.adele.cilia.admin", null);
	}

	private Mediator getRemoverMediator(){
		return new Mediator("admin-remove", "CiliaAdminRemover","fr.liglab.adele.cilia.admin", null);
	}

	private Mediator getShowMediator(){
		return new Mediator("admin-show", "CiliaAdminShow","fr.liglab.adele.cilia.admin", null);
	}

	private Mediator getStarterMediator(){
		return  new Mediator("admin-starter", "CiliaAdminStarter","fr.liglab.adele.cilia.admin", null);
	}

	private Mediator getStopMediator(){
		return new Mediator("admin-stop", "CiliaAdminStop","fr.liglab.adele.cilia.admin", null);
	}
	
	private Mediator getModifierMediator(){
		return new Mediator("admin-modify", "CiliaAdminModifier","fr.liglab.adele.cilia.admin", null);
	}

	private Mediator getLoaderMediator(){
		return new Mediator("admin-loader", "CiliaAdminLoader","fr.liglab.adele.cilia.admin", null);
	}
	
	private Mediator getReplacerMediator() {
		return new Mediator("admin-replacer", "CiliaAdminReplacer","fr.liglab.adele.cilia.admin", null);		
	}
	private Mediator getCopierMediator() {
		return new Mediator("admin-copier", "CiliaAdminCopier","fr.liglab.adele.cilia.admin", null);		
	}
}
