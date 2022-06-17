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
		capability "Polling"
		capability "Refresh"
		capability "Lock"
	}
}

// handle commands
def poll() {
	parent.poll()
}

def refresh() {
	poll()
}

def unlock() {
	if (parent.postService("/api/services/lock/unlock", ["entity_id": device.deviceNetworkId])) {
    	sendEvent(name: "lock", value: "unlocked")
    }
}

def lock() {
	if (parent.postService("/api/services/lock/lock", ["entity_id": device.deviceNetworkId])) {
    	sendEvent(name: "lock", value: "locked")
    }
}