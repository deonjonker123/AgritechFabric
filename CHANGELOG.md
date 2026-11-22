# Changelog

All notable changes to this project will be documented here.

## [1.0.4.3+fabric-mc26.1.2] - 2026-05-26
### Fixed
- Fallback recipe for planters, raised beds, and crates

## [1.0.4.5+fabric-mc26.1.2] - 2026-05-26
### Changed
- Reverted to more vanilla-esque GUI textures

## [1.1.0+fabric-mc26.1.2] - 2026-06-18
### Changed
- Compatibility layer is now fully data-driven. Seeds, saplings, soils, fertilizers and their values are defined via recipes and datamaps — fully packdev-accessible via datapacks or KubeJS.
### Added
- Tooltip injection on all valid soil blocks and fertilizers showing their planter growth modifiers

## [1.1.0.1+fabric-mc26.1.2] - 2026-06-24
### Fixed
- Fixed Farmer's Delight's Rich Soil to Rich Soil Farmland hoe tilling not working in planters and raised beds

## [1.1.0.2+fabric-mc26.1.2] - 2026-06-24
### Fixed
- Added Biomes O' Plenty, Oh The Biomes We've Gone, and Regions Unexplored, compatibility

## [1.1.0.4+fabric-mc26.1.2] - 2026-08-29
### Added
- New vanilla crops compatability: all coral, short grass, and ferns

### Changed

- Updated basic planters' recipes to be less annoying 

## [1.1.0.5+fabric-mc26.1.2] - 2026-11-22
### Added
- Cloche domes with its config and speed/yield bonus (#2)

### Fixed
- Servers: Soil/Fertilizer tooltips now show on the client
- Servers: seeds/soils/fertilizers no longer flicker/fail when placing or shift-clicking
- Full planters no longer delete harvested items when there is no more space
- Datapacks adding to fertilizer/soil maps now merge with the defaults and other datapacks instead of replacing them
- Planter contents and the cloche now drops contents when broken by any non-player means
- Planters can now push items into modded containers (#3)
- Network traffic performance improvements
- Slightly improved harvest logic
- Crates now respects item pickup delay on player-dropped items