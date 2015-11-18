/**
 *  Mode Cleverness
 *
 *  Copyright 2015 Brian Steere
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
    name: "Mode Cleverness",
    namespace: "dianoga",
    author: "Brian Steere",
    description: "Change modes based on my very specific criteria",
    category: "Mode Magic",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	page(name: "settings")
}

def settings() {
	dynamicPage(name: "settings", title: "Settings", install: true, uninstall: true) {
        def actions = location.helloHome?.getPhrases()*.label
		if (actions) actions.sort()
    
        section("People") {
            input "people", "capability.presenceSensor", title: "Who should we keep track of?", multiple: true
        }

        section("Present/Day") {
            input "presentDayMode", "mode", title: "Mode"
            if (actions) input "presentDayAction", "enum", title: "Action", options: actions
        }
        
        section("Present/Evening") {
            input "presentEveningMode", "mode", title: "Mode"
            if (actions) input "presentEveningAction", "enum", title: "Action", options: actions
		}
        
        section("Present/Night") {
            input "presentNightMode", "mode", title: "Mode"
            if (actions) input "presentNightAction", "enum", title: "Action", options: actions
        }
        
        section("Away/Day") {
            input "awayDayMode", "mode", title: "Mode"
            if (actions) input "awayDayAction", "enum", title: "Action", options: actions
        }
        
        section("Away/Evening") {
            input "awayEveningMode", "mode", title: "Mode"
            if (actions) input "awayEveningAction", "enum", title: "Action", options: actions
		}
        
        section("Away/Night") {
            input "awayNightMode", "mode", title: "Mode"
            if (actions) input "awayNightAction", "enum", title: "Action", options: actions
        }
    }
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
    subscribe(people, "presence", "presenceHandler")
}

def presenceHandler(event) {
    String present = people.find { it.currentPresence == 'present' }

    def s = getSunriseAndSunset()
    def now = new Date()
    def tenpm = timeToday('22:00', location.timeZone)
    
    String mode
    String action
    
    if(present) {
        if(now.before(s.sunrise) || now.after(tenpm)) {
            mode = settings.presentNightMode
            action = settings.presentNightAction
        } else if(now.after(s.sunset)) {
            mode = settings.presentEveningMode
            action = settings.presentEveningAction
        } else {
            mode = settings.presentDayMode
            action = settings.presentDayAction
        }
    } else {
        if(now.before(s.sunrise) || now.after(tenpm)) {
            mode = settings.awayNightMode
            action = settings.awayNightAction
        } else if(now.after(s.sunset)) {
            mode = settings.awayEveningMode
            action = settings.awayEveningAction
        } else {
            mode = settings.awayDayMode
            action = settings.awayDayAction
        }
    }

    log.debug "Mode: ${mode} | Action: ${action}"
    location.setMode(mode)
    if(action) location.helloHome?.execute(action)
}
