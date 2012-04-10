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

public interface ChainRuntime {
	/* The Chain has never received a start or a stop command */
	public static final int STATE_IDLE = 0;
	/* the chain has received a command 'startChain' */
	public static final int STATE_STARTED =1 ;
	/* the chain has received a command 'stopChain' */
	public static final int STATE_STOPPED =2 ;
	
	public String getUUID() ;
	
	/* return the current state of the Chain */
	public int getState() ;

}