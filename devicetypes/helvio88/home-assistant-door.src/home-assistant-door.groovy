/**
 *  Home Assistant Door
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
	definition (name: "Home Assistant Door", namespace: "Helvio88", author: "Helvio Pedreschi") {
		capability "Actuator"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Door Control"
	}


	simulator { }

	tiles(scale: 2) {
		standardTile("toggle", "device.door", width: 2, height: 2) {
			state("closed", label:'${name}', action:"door control.open", icon:"st.doors.garage.garage-closed", backgroundColor:"#00A0DC", nextState:"opening")
			state("open", label:'${name}', action:"door control.close", icon:"st.doors.garage.garage-open", backgroundColor:"#e86d13", nextState:"closing")
			state("opening", label:'${name}', icon:"st.doors.garage.garage-closed", backgroundColor:"#e86d13")
			state("closing", label:'${name}', icon:"st.doors.garage.garage-open", backgroundColor:"#00A0DC")
			
		}
		standardTile("open", "device.door", inactiveLabel: false, decoration: "flat") {
			state "default", label:'open', action:"door control.open", icon:"st.doors.garage.garage-opening"
		}
		standardTile("close", "device.door", inactiveLabel: false, decoration: "flat") {
			state "default", label:'close', action:"door control.close", icon:"st.doors.garage.garage-closing"
		}

		main "toggle"
		details(["toggle", "open", "close"])
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
	if (parent.postService("/api/services/cover/open_cover", ["entity_id": device.deviceNetworkId])) {
    	sendEvent(name: "switch", value: "on")
    }
}

def close() {
	if (parent.postService("/api/services/cover/close_cover", ["entity_id": device.deviceNetworkId])) {
    	sendEvent(name: "switch", value: "off")
    }
}

def presetPosition(percent) {
	def state = (percent == 0 ? "off" : "on")
    
    if (parent.postService("/api/services/cover/set_cover_position", ["entity_id": device.deviceNetworkId, "position": percent])) {
    	sendEvent(name: "level", value: percent)
    	sendEvent(name: "switch.setLevel", value: percent)
        sendEvent(name: "switch", value: state)
    }
}

def on() {
	open()
}

def off() {
	close()
}

def setLevel(percent) {
	presetPosition(percent)
}