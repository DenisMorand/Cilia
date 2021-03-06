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

package fr.liglab.adele.cilia.framework.monitor.statevariable;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.metadata.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.utils.Const;
import fr.liglab.adele.cilia.framework.utils.WorkQueue;
import fr.liglab.adele.cilia.management.Watch;

public class MonitorHandlerStateVar extends AbstractStateVariable {
	private static Logger logger = LoggerFactory.getLogger(Const.LOGGER_CORE);
	/* TAG for storing message history */
	private static final String PROPERTY_MSG_HISTORY = "cilia.message.history";
	private static final String PROPERTY_BINDING_TIME = "cilia.message.time.bind";
	/* Liste of state var */
	private static final Set setStateVar, setDependencyCall, setEventing, setSystemCall;

	static {
		/* State var Event fired by dependency Manager */
		setDependencyCall = new HashSet();
		setDependencyCall.add("service.arrival");
		setDependencyCall.add("service.departure");
		setDependencyCall.add("service.arrival.count");
		setDependencyCall.add("service.departure.count");

		/* state var type eventing */
		setEventing = new HashSet();
		setEventing.add("fire.event");
		setEventing.add("fire.event.count");

		/* State var type SyemCall */
		setSystemCall = new HashSet();
		/* Phase Collect */
		setSystemCall.add("scheduler.count");
		setSystemCall.add("scheduler.data");
		/* Phase Processing */
		setSystemCall.add("process.entry.count");
		setSystemCall.add("process.entry.data");
		setSystemCall.add("process.exit.count");
		setSystemCall.add("process.exit.data");
		setSystemCall.add("process.err.count");
		setSystemCall.add("process.err.data");
		setSystemCall.add("process.msg.treated");
		/* number of ticks for the phase Process */
		setSystemCall.add("processing.delay");
		/* Phase dispatching */
		setSystemCall.add("dispatch.count");
		setSystemCall.add("dispatch.data");
		setSystemCall.add("dispatch.msg.treated");
		/* Message history */
		setSystemCall.add("message.history");
		/* time between dispatch and collect */
		setSystemCall.add("transmission.delay");

		/* All state variables */
		setStateVar = new HashSet(setSystemCall);
		setStateVar.addAll(setDependencyCall);
		setStateVar.addAll(setEventing);

	}

	/* This reference will be injected by iPOJO */
	private WorkQueue m_systemQueue;
	private long[] m_counters = new long[10];
	private LinkedList m_gatherMsgIn = new LinkedList();
	private LinkedList m_snapshootMsg = new LinkedList();
	private LinkedList m_historyList = new LinkedList();
	private Object _lock = new Object();
	private Watch processTime;

	public void configure(Element metadata, Dictionary configuration)
			throws ConfigurationException {
		super.configure(metadata, configuration);
		Iterator it;
		/* Configure by default only state var in the dsl */
		Map stateVarList = (Map) configuration.get("state.variable.configuration");
		if (stateVarList != null) {
			it = stateVarList.keySet().iterator();
			while (it.hasNext()) {
				String id = (String) it.next();
				if ((id != null) && setStateVar.contains(id)) {
					addStateVarId(id, (String) configuration.get(id));
				}
			}
		} else {
			/* No configuration , configure all system state-variables */
			it = setStateVar.iterator();
			while (it.hasNext()) {
				String id = (String) it.next();
				addStateVarId(id, null);
			}
		}
		/* set enabled status, default = enabled */
		//String enabled = (String) configuration.get("state.variable.status");
		//if (enabled != null) {
		//	isStateVarEnable = enabled.equalsIgnoreCase("true");
		//} else {
		//	isStateVarEnable = false;
		//}
	}

	public void validate() {
		super.validate();
	}

	public void unvalidate() {
		super.unvalidate();
	}

	private void gatherIncommingHistory(Data data) {
		if (data != null) {
			synchronized (_lock) {
				/* Gather all messages history */
				List list = (List) data.getProperty(PROPERTY_MSG_HISTORY);
				/* Watch == null means , first incoming message */
				if (list != null)
					m_gatherMsgIn.addAll(list);
			}
		}
	}

	private void snapShotHistory() {
		synchronized (_lock) {
			if (!m_gatherMsgIn.isEmpty())
				m_snapshootMsg.addAll(m_gatherMsgIn);
			m_gatherMsgIn.clear();
		}
	}

	/*
	 * Injects tag for - message history - compute binding time
	 */
	private void injectTags(List listData) {
		Iterator it;
		Watch watch;
		synchronized (_lock) {
			if (!m_snapshootMsg.isEmpty()) {
				m_historyList.addAll(m_snapshootMsg);
				m_snapshootMsg.clear();
			}
			watch = new Watch(getId());
			m_historyList.addLast(watch);
		}
		if (!m_historyList.isEmpty()) {
			it = listData.iterator();
			while (it.hasNext()) {
				Data data = (Data) it.next();
				data.setProperty(PROPERTY_MSG_HISTORY, new LinkedList(m_historyList));
				data.setProperty(PROPERTY_BINDING_TIME, watch);
			}
			m_historyList.clear();
		}
	}

	public void onCollect(Data data) {
		if (m_listStateVarEnable.isEmpty())
			return;

		gatherIncommingHistory(data);

		if (isEnabled("scheduler.count")) {
			m_counters[0]++;
			m_systemQueue.execute(new AsynchronousExec("scheduler.count", m_counters[0]));
		}
		if (isEnabled("scheduler.data")) {
			m_systemQueue.execute(new AsynchronousExec("scheduler.data", data));
		}
		/* Computes the binding time */
		if (isEnabled("transmission.delay")) {
			if (data != null) {
				synchronized (_lock) {
					Watch watch = (Watch) data.getProperty(PROPERTY_BINDING_TIME);
					data.removeProperty(PROPERTY_BINDING_TIME);
					if (watch != null) {
						long elapsedTime = Watch.fromTicksToMs(watch.getElapsedTicks());
						m_systemQueue.execute(new AsynchronousExec("transmission.delay",
								elapsedTime));
					}
				}
			}
		}
		/* Publish data history */
		if (isEnabled("message.history")) {
			/* Retreive history */
			List list = (List) data.getProperty(PROPERTY_MSG_HISTORY);
			if ((list != null) && (!list.isEmpty())) {
				Data history = new Data(list);
				m_systemQueue.execute(new AsynchronousExec("message.history", history));
			}
		}
	}

	public void onProcessEntry(List data) {

		if (m_listStateVarEnable.isEmpty())
			return;
		
		snapShotHistory();

		processTime = new Watch();
		if (isEnabled("process.entry.count")) {
			m_counters[1]++;
			m_systemQueue.execute(new AsynchronousExec("process.entry.count",
					m_counters[1]));
		}
		if (isEnabled("process.entry.data")) {
			m_systemQueue.execute(new AsynchronousExec("process.entry.data", data));
		}
		if (isEnabled("process.msg.treated")) {
			/* # number of messages treated */
			if (data != null)
				m_counters[8] += data.size();
			m_systemQueue.execute(new AsynchronousExec("process.msg.treated",
					m_counters[8]));
		}
	}

	public void onProcessExit(List data) {

		if (m_listStateVarEnable.isEmpty())
			return;
		
		if (isEnabled("message.history") || isEnabled("transmission.delay"))
			injectTags(data);

		if (isEnabled("processing.delay")) {
			m_systemQueue.execute(new AsynchronousExec("processing.delay", Watch
					.fromTicksToMs(processTime.getElapsedTicks())));
		}
		if (isEnabled("process.exit.count")) {
			m_counters[2]++;
			m_systemQueue.execute(new AsynchronousExec("process.exit.count",
					m_counters[2]));
		}

	}

	public void onDispatch(List data) {
		if (m_listStateVarEnable.isEmpty())
			return;

		if (isEnabled("dispatch.count")) {
			m_counters[3]++;
			m_systemQueue.execute(new AsynchronousExec("dispatch.count", m_counters[3]));
		}
		if (isEnabled("dispatch.data")) {
			m_systemQueue.execute(new AsynchronousExec("dispatch.data", data));
		}
		if (isEnabled("dispatch.msg.treated")) {
			if (data != null)
				m_counters[9] += data.size();
			m_systemQueue.execute(new AsynchronousExec("dispatch.msg.treated",
					m_counters[9]));
		}

	}

	public void onProcessError(Data data, Exception ex) {
		if (m_listStateVarEnable.isEmpty())
			return;

		m_counters[4]++;
		if (isEnabled("process.err.count")) {
			m_counters[4]++;
			m_systemQueue
					.execute(new AsynchronousExec("process.err.count", m_counters[4]));
		}
		if (isEnabled("process.err.data")) {
			m_systemQueue.execute(new AsynchronousExec("process.err.data", data));
		}
	}

	/*
	 * receive events from the framework
	 */
	public void fireEvent(Map info) {
		if (m_listStateVarEnable.isEmpty())
			return;

		if (isEnabled("fire.event")) {
			m_systemQueue.execute(new AsynchronousExec("fire.event", info));
		}
		if (isEnabled("fire.event.count")) {
			m_counters[5]++;
			m_systemQueue
					.execute(new AsynchronousExec("fire.event.count", m_counters[5]));
		}
	}

	/*
	 * receive event service from dependency handler
	 */
	public void onServiceArrival(Map info) {
		if (m_listStateVarEnable.isEmpty())
			return;

		if (isEnabled("service.arrival")) {
			m_systemQueue.execute(new AsynchronousExec("service.arrival", info));
		}
		if (isEnabled("service.arrival.count")) {
			m_counters[6]++;
			m_systemQueue.execute(new AsynchronousExec("service.arrival.count",
					m_counters[6]));
		}
	}

	/*
	 * receive event service form dependency handler
	 */
	public void onServiceDeparture(Map info) {
		if (m_listStateVarEnable.isEmpty())
			return;

		if (isEnabled("service.departure")) {
			m_systemQueue.execute(new AsynchronousExec("service.departure", info));
		}
		if (isEnabled("service.departure.count")) {
			m_counters[7]++;
			m_systemQueue.execute(new AsynchronousExec("service.departure.count",
					m_counters[7]));
		}
	}

	public String[] getCategories() {
		String[] array = { "SystemCall", "DependencyCall", "EventingCall" };
		return array;
	}

	@SuppressWarnings("unchecked")
	public String[] getStateVarIdCategory(String category) {
		String[] array;
		if (category.equalsIgnoreCase("SystemCall"))
			array = (String[]) setSystemCall.toArray(new String[setSystemCall.size()]);
		else if (category.equalsIgnoreCase("DependencyCall")) {
			array = (String[]) setDependencyCall.toArray(new String[setDependencyCall
					.size()]);
		} else if (category.equalsIgnoreCase("EventingCall")) {
			array = (String[]) setEventing.toArray(new String[setEventing.size()]);
		} else
			array = new String[0];

		return array;
	}

	/**
	 * Asynchronous execution
	 * 
	 */
	private class AsynchronousExec implements Runnable {
		private String stateVar;
		private Object data = null;
		private long value = 0;
		private final long tickCount = Watch.getCurrentTicks();
		private final int type;

		AsynchronousExec(String stateVar, long value) {
			this.stateVar = stateVar;
			this.value = value;
			type = 1;
		}

		AsynchronousExec(String stateVar, Object data) {
			this.stateVar = stateVar;
			this.data = data;
			type = 2;
		}

		/* Asynchronous event execution */
		public void run() {

			switch (type) {
			case 1:
				publish(stateVar, value, tickCount);
				break;
			case 2:
				publish(stateVar, data, tickCount);
				break;
			}
		}
	}

}
