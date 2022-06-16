/**
 *  Home Assistant Lock
 *
 *  Copyright 2022 Helvio Pedreschi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Home Assistant Lock", namespace: "Helvio88", author: "Helvio Pedreschi") {
		capability "Actuator"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Lock"
	}


	simulator { }

	tiles(scale: 2) {
    	multiAttributeTile(name:"lock", type: "switch", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.lock", key: "PRIMARY_CONTROL") {
				attributeState "unlocked", label:'${name}', action:"close", icon:"st.Home.home9", backgroundColor:"#00A0DC", nextState:"locked"
				attributeState "locked", label:'${name}', action:"open", icon:"st.Home.home9", backgroundColor:"#ffffff", nextState:"unlocked"
			}
			
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"presetPosition"
			}
		}
    
		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

        main(["lock"])
    	details(["rich-conntrol", "refresh"])

	}
}

// handle commands
def poll() {
	parent.poll()
}

def refresh() {
	poll()
}

def open() {
	if (parent.postService("/api/services/lock/unlock", ["entity_id": device.deviceNetworkId])) {
    	sendEvent(name: "switch", value: "on")
    }
}

def close() {
	if (parent.postService("/api/services/lock/lock", ["entity_id": device.deviceNetworkId])) {
    	sendEvent(name: "switch", value: "off")
    }
}

def on() {
	open()
}

def off() {
	close()
}