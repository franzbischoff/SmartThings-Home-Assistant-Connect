/**
 *  SmartThings Home Assistant Connect
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
definition(
    name: "SmartThings Home Assistant Connect",
    namespace: "Helvio88",
    author: "Helvio Pedreschi",
    description: "Connect your Home Assistant devices to SmartThings.",
    category: "My Apps",
    iconUrl: "https://raw.githubusercontent.com/home-assistant/assets/master/logo/logo-small.png",
    iconX2Url: "https://raw.githubusercontent.com/home-assistant/assets/master/logo/logo-pretty.png",
    iconX3Url: "https://raw.githubusercontent.com/home-assistant/assets/master/logo/logo-pretty.png") {
    appSetting "hassUrl"
    appSetting "token"
}


preferences {
    page(name: "setup", content: "setupPage")
}

def installed() {
    log.debug "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unsubscribe()
    initialize()
}

def initialize() {
    log.debug "initialize"

    addChildren(doors ?: [], state.entities["doors"], "Home Assistant Door")
    addChildren(fans ?: [], state.entities["fans"], "Home Assistant Fan")
    addChildren(lights ?: [], state.entities["lights"], "Home Assistant Light")
    addChildren(locks ?: [], state.entities["locks"], "Home Assistant Lock")
    addChildren(scripts ?: [], state.entities["scripts"], "Home Assistant Script")
    addChildren(switches ?: [], state.entities["switches"], "Home Assistant Switch")

    // Delete any that are no longer selected
    log.debug "selected devices: ${settings.collectMany { it.value }}"
    def delete = getChildDevices().findAll { !settings.collectMany { it.value }.contains(it.getDeviceNetworkId()) }
    log.warn "delete: ${delete}, deleting ${delete.size()} devices"
    delete.each { deleteChildDevice(it.getDeviceNetworkId()) }

    // Polling
    poll()
    runEvery5Minutes("poll")
}

def setupPage() {
    log.debug "setupPage"
    def options = getOptions()

    return dynamicPage(name: "setup", title: "Home Assistant", install: true, uninstall: true) {
        section {
            paragraph "Tap below to see the list of devices available in Home Assistant and select the ones you want to connect to SmartThings."
            input(name: "doors", type: "enum", required: false, title: "Doors", multiple: true, options: options.doors)
            input(name: "fans", type: "enum", required: false, title: "Fans", multiple: true, options: options.fans)
            input(name: "lights", type: "enum", required: false, title: "Lights", multiple: true, options: options.lights)
            input(name: "locks", type: "enum", required: false, title: "Locks", multiple: true, options: options.locks)
            input(name: "scripts", type: "enum", required: false, title: "Scripts", multiple: true, options: options.scripts)
            input(name: "switches", type: "enum", required: false, title: "Switches", multiple: true, options: options.switches)
        }
    }
}

// Get entities from Home Assistant
def getEntities() {
    log.debug "getEntities"

    def params = [
        uri: appSettings.hassUrl,
        path: "/api/states",
        headers: ["Authorization": "Bearer " + appSettings.token],
        contentType: "application/json"
    ]

    def entities = [:]

    try {
        httpGet(params) { resp ->
            // Doors
            def doors = [:]
            resp.data.findAll { 
                it.entity_id.startsWith("cover.") 
            }.each {
                doors["${it.entity_id}"] = it
            }
            entities["doors"] = doors

            // Fans
            def fans = [:]
            resp.data.findAll { 
                it.entity_id.startsWith("fan.") 
            }.each {
                fans["${it.entity_id}"] = it
            }
            entities["fans"] = fans

            // Lights
            def lights = [:]
            resp.data.findAll { 
                it.entity_id.startsWith("light.") 
            }.each {
                lights["${it.entity_id}"] = it
            }
            entities["lights"] = lights

            // Locks
            def locks = [:]
            resp.data.findAll { 
                it.entity_id.startsWith("lock.") 
            }.each {
                locks["${it.entity_id}"] = it
            }
            entities["locks"] = locks

            // Scripts
            def scripts = [:]
            resp.data.findAll { 
                it.entity_id.startsWith("script.") 
            }.each {
                scripts["${it.entity_id}"] = it
            }
            entities["scripts"] = scripts

            // Switches
            def switches = [:]
            resp.data.findAll { 
                it.entity_id.startsWith("switch.") 
            }.each {
                switches["${it.entity_id}"] = it
            }
            entities["switches"] = switches

            state.entities = entities
            return entities
        }
    } catch (e) {
        log.error "something went wrong: $e"
    }
}

// Populate Smartapp setup page with Home Assistant entities
def getOptions() {
    getEntities()
    def options = [:]

    state.entities.each { domain, domainEntities ->
        def values = [:]

        domainEntities.each { entityId, entity ->
            values[entityId] = entity.attributes.friendly_name
        }

        values = values.sort { it.value }
        options["${domain}"] = values
    }

    return options
}

def addChildren(chosenEntities, domain, deviceType) {
    log.debug "addChildren"

    // Create devices for newly selected Home Assistant entities
    chosenEntities.each { entityId ->
        if (!getChildDevice(entityId)) {
            device = addChildDevice(app.namespace, deviceType, entityId, null, 
                [name: "Device.${entityId}", label:"${domain[entityId].attributes.friendly_name}", completedSetup: true])
            log.debug "created ${device.displayName} with id ${device.getDeviceNetworkId()}"
        }
    }
}

// Poll child devices
def poll() {
    getEntities()
    def devices = getChildDevices()

    // Doors
    devices.findAll {
        it.getTypeName() == "Home Assistant Door"
    }.each { device ->
        def entityId = device.getDeviceNetworkId()
        def entity = state.entities.doors[entityId]

        device.sendEvent(name: "door", value: entity.state)
        device.sendEvent(name: "label", entity.attributes.friendly_name)
    }

    // Fans
    devices.findAll {
        it.getTypeName() == "Home Assistant Fan"
    }.each { device ->
        def entityId = device.getDeviceNetworkId()
        def entity = state.entities.fans[entityId]

        device.sendEvent(name: "switch", value: entity.attributes.state)
        device.sendEvent(name: "level", value: entity.attributes.percentage)
        device.sendEvent(name: "label", entity.attributes.friendly_name)
    }

    // Lights
    devices.findAll {
        it.getTypeName() == "Home Assistant Light"
    }.each { device ->
        def entityId = device.getDeviceNetworkId()
        def entity = state.entities.lights[entityId]

        if (entity.attributes.rgb_color) {
            device.sendEvent(name: "color", value: colorUtil.rgbToHex(entity.attributes.rgb_color[0], entity.attributes.rgb_color[1], entity.attributes.rgb_color[2]))
        }

        if (entity.attributes.color_temp) {
            device.sendEvent(name: "colorTemperature", value: (1000000).intdiv(entity.attributes.color_temp))
        }

        if (entity.attributes.brightness) {
            device.sendEvent(name: "level", value: entity.attributes.brightness / 255 * 100)
            device.sendEvent(name: "switch.setLevel", value: entity.attributes.brightness / 255 * 100)
        }

        device.sendEvent(name: "switch", value: entity.state)
        device.sendEvent(name: "label", value: entity.attributes.friendly_name)
    }

    // Locks
    devices.findAll {
        it.getTypeName() == "Home Assistant Lock"
    }.each { device ->
        def entityId = device.getDeviceNetworkId()
        def entity = state.entities.locks[entityId]

        device.sendEvent(name: "lock", value: entity.state)
        device.sendEvent(name: "label", entity.attributes.friendly_name)
    }

    // Switches
    devices.findAll {
        it.getTypeName() == "Home Assistant Switch"
    }.each { device ->
        def entityId = device.getDeviceNetworkId()
        def entity = state.entities.subMap(["switches"]).collectEntries { it.value }[entityId]

        device.sendEvent(name: "switch", value: entity.state)
        device.sendEvent(name: "label", value: entity.attributes.friendly_name)
    }

    // Scripts
    devices.findAll {
        it.getTypeName() == "Home Assistant Script"
    }.each { device ->
        def entityId = device.getDeviceNetworkId()
        def entity = state.entities.subMap(["scripts"]).collectEntries { it.value }[entityId]

        device.sendEvent(name: "script", value: entity.state)
        device.sendEvent(name: "label", value: entity.attributes.friendly_name)
    }
}

// Call Home Assistant services via HTTP POST request
def postService(service, data) {
    def params = [
        uri: appSettings.hassUrl,
        path: service,
        headers: ["Authorization": "Bearer " + appSettings.token],
        requestContentType: "application/json",
        body: data
    ]

    try {
        httpPost(params) { resp ->
            return true
        }
    } catch (e) {
        log.error "something went wrong: $e"
        return false
    }
}