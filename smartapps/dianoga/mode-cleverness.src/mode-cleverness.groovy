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
	section("People") {
		input "people", "capability.presenceSensor", title: "Who should we keep track of?", multiple: true
	}
    
    section("Modes when present") {
    	input "presentDay", "mode", title: "Day"
        input "presentEvening", "mode", title: "Evening"
        input "presentNight", "mode", title: "Night"
    }
    
    section("Modes when away") {
    	input "awayDay", "mode", title: "Day"
        input "awayEvening", "mode", title: "Evening"
        input "awayNight", "mode", title: "Night"
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

    if(present) {
        if(now.before(s.sunrise) || now.after(tenpm)) {
            mode = settings.presentNight
        } else if(now.after(s.sunset)) {
            mode = settings.presentEvening
        } else {
            mode = settings.presentDay
        }
    } else {
        if(now.before(s.sunrise) || now.after(tenpm)) {
            mode = settings.awayNight
        } else if(now.after(s.sunset)) {
            mode = settings.awayEvening
        } else {
            mode = settings.awayDay
        }
    }

    log.debug "Changing mode to ${mode}"
    location.setMode(mode)
}
