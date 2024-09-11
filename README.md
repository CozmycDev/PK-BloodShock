[![GitHub Pre-Release](https://img.shields.io/github/release-pre/CozmycDev/PK-BloodShock.svg)](https://github.com/CozmycDev/PK-BloodShock/releases)
[![Github All Releases](https://img.shields.io/github/downloads/CozmycDev/PK-BloodShock/total.svg)](https://github.com/CozmycDev/PK-BloodShock/releases)
![Size](https://img.shields.io/github/repo-size/CozmycDev/PK-BloodShock.svg)

# BloodShock Ability for ProjectKorra

This is an addon ability for the [ProjectKorra](https://projectkorra.com/) plugin for Spigot Minecraft servers. Ability concept by [LuxaelNi](https://github.com/LuxaelNivra).

https://github.com/user-attachments/assets/61d66c5a-4324-4167-ae60-84b2ab41bb0b

## Description

**BloodShock** is a Blood ability that allows waterbenders to bend the blood of every entity around them at once. This overpowered ability lets you control their movements, and/or you can toss them all away from you.

### Features

- **AoE Bloodbending**: Bloodbend multiple opponents at the same time!
- **Control Movements**: Make your opponents jump around or do the cha cha slide, involuntarily! 
- **Toss**: Toss them all away with a configurable strength.
- **Bloodbending Configuration**: Uses the global Bloodbending configuration `Abilities.Water.Bloodbending`, to respect day time rules, full moon, etc.

## Instructions

- **Activation**: During the right conditions for bloodbending, hold Shift to activate. Left Click to raise enemies into the air. Release Shift to toss them all away.

## Installation

1. Download the latest `bloodshock.jar` file from [releases](https://github.com/CozmycDev/PK-BloodShock/releases).
2. Place the latest `bloodshock.jar` file in the `./plugins/ProjectKorra/Abilities` directory.
3. Restart your server or reload the ProjectKorra plugin with `/b reload` to enable the ability.

## Compatibility

- **Minecraft Version**: Tested and working on MC 1.20.4.
- **ProjectKorra Version**: Tested and working on PK 1.11.2 and 1.11.3. Might support earlier versions too.

## Configuration

The ability can be configured in the ProjectKorra `config.yml` file under `ExtraAbilities.Cozmyc.BloodShock`:

```yaml
ExtraAbilities:
  Cozmyc:
    BloodShock:
      ControlMovements: true
      Cooldown: 5000
      Duration: 6000
      LiftHeight: 2.5
      ThrowPower: 2.0
      ThrowHeight: 0.7
      Range: 10
      Language:
        Description: Extremely skilled bloodbenders have demonstrated the ability
          of taking full control of any living being in their surrounding area. This
          ability grants the user mass crowd control, allowing them to forcefully
          make entities mimic the bender's movements until they are lifted into the
          air and launched away upon release.
        Instructions: Hold Shift to activate, Left Click to lift entities, release
          Shift to launch.
      NightOnly: true
      FullMoonOnly: false
      UndeadMobs: true
      OtherBloodbenders: false
      Bloodless: false
