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

/**
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class Port {

   /**
    * Port Name. 
    */
    private final String portName;
    /**
     * The allowed data type.
     */
    private final String dataType;
    
    private final static String ANY_DATA_TYPE = "any";
    /**
     * Port Type, ex INPUT/OUTPUT
     */
    private final PortType portType;
    /**
     * Mediator which contain this port.
     */
    private final MediatorComponent mediator;
    /**
     * This constructor will create a Port which will be asociated to the
     * given mediator.
     * @param name Name of the port.
     * @param ptype Type of port.
     * @param mediator Mediator which contain this port.
     */
    protected Port (String name, PortType ptype, MediatorComponent med ) {
        this(name, ANY_DATA_TYPE, ptype, med);
    }
    
    protected Port (String name, String dt, PortType ptype, MediatorComponent med ) {
        portName = name;
        portType = ptype;
        mediator = med;
        dataType = dt;
    }
    /**
     * Get the port Tyoe.
     * @return
     */
    public PortType getType() {
        return portType;
    }
    /**
     * Get the port name.
     * @return
     */
    public String getName() {
        return portName;
    }
    /**
     * Get the mediator reference which contain this port.
     * @return
     */
    public MediatorComponent getMediator() {
        return mediator;
    }
    
    
}


