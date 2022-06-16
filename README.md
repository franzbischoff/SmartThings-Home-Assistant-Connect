# SmartThings-Home-Assistant-Connect

This SmartApp allows you to connect your existing Home Assistant devices to SmartThings.

This is a fork from @gurase and I intend to add additional device types, as well as separate Scrips from Switches because I don't think they work the same way.

I am picking up this project because I bought a Samsung Smart Fridge and would like to use the Widget to control my smart devices

## Roadmap:
- Fan
- Vacuum
- Automation

## Supported Devices, Features, and Limitations
Currently cover, light, script, and switch device types are supported.

- **cover** - Like emulated_hue, cover devices are treated like lights, so you have to say "turn on the shades" to open them, etc. Also supports setting the position.
- **light** - All lights are treated like colored bulbs. You are able to use voice control to set the color.
- **script** - On/Off. Will attempt to change to a stateless button.
- **switch** - On and Off.
- **lock** - Lock and Unlock.

You can use the **smartthings_name** attribute in Home Assistant to set a custom name for your device in SmartThings. Otherwise, **friendly_name** will be used.

## Installation
1. Install and publish the Smart App in the Smart App IDE using "Create via code".
1. Under Settings in the Smart App IDE, add the following App Settings:
   - **token** - a long-lived access token, created in your Home Assistant user account
   - **hassUrl** - your Home Assistant URL (No Trailing Slash)
1. Install and publish all Device Handlers in the Device Handler IDE using "Create via code".
1. Open the SmartThings app on your phone, and install the Home Assistant Connect SmartApp Automations - Add Routine (Scroll to the bottom).
1. Select all the Home Assistant devices you would like to connect to SmartThings.
1. You should now be able to control your Home Assistant devices from SmartThings!
