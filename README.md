# BloodShock Ability for ProjectKorra

This is an addon ability for the [ProjectKorra](https://projectkorra.com/) plugin for Spigot Minecraft servers. Ability concept by [LuxaelNi](https://github.com/LuxaelNivra).

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
      ControlMovements: true  # whether you can control entities movements while holding shift and before clicking
      Cooldown: 5000  # milliseconds
      Duration: 6000
      LiftHeight: 2.5
      LaunchPower: 2.0
      LaunchHeight: 0.7
      Range: 10
      UseBloodbendingAbilityConfig: true