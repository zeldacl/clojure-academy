# Cat Engine Block Architecture

## Overview
The Cat Engine is a specialized energy generator block that integrates with the wireless energy network system. It follows a three-layer architecture pattern consisting of block logic, tile entity, and GUI components.

## Core Components

### 1. Block Implementation (core.clj)
The block layer handles the core block behavior and player interactions:

- **ICatEngineBlock Protocol**
  - create-tile-entity: Creates the tile entity instance
  - on-block-activated: Handles player interaction for linking/unlinking with wireless networks
  - is-opaque-cube?: Controls block rendering properties
  - get-render-type: Defines the rendering behavior

- **Key Features**
  - Network discovery and auto-linking
  - Player feedback messaging
  - Non-opaque rendering

### 2. Tile Entity (tile.clj)
Manages the block's state and energy generation:

- **Constants**
  - ENERGY_MAX: 100,000.0
  - ENERGY_GEN_PER_TICK: 5.0
  - ENERGY_BANDWIDTH: 200.0

- **State Management**
  - Energy level tracking
  - Rotation animation
  - Generation status
  - Network connectivity

- **Interfaces**
  - ICatEngineTile: Core tile entity operations
  - INBTSerializable: Data persistence
  - IWirelessGenerator: Energy generation capabilities

### 3. GUI Component (gui.clj)
Provides visual interface for monitoring the engine:

- **Visual Elements**
  - Energy level display
  - Generation rate indicator
  - Network connection status
  - Background texture (176x166)

- **Interface Components**
  - Energy information panel
  - Network status display
  - Interactive elements

## State Management
- Uses atom-based state management
- Persistent across game sessions via NBT serialization
- Real-time updates for energy generation

## Network Integration
- Seamless integration with wireless energy network
- Auto-discovery of nearby network nodes
- Dynamic linking/unlinking capability
- Energy transfer through wireless network

## Technical Specifications
- Energy Generation: 5.0 units per tick
- Maximum Energy Storage: 100,000.0 units
- Energy Transfer Bandwidth: 200.0 units
- Block Type: Non-opaque, Special renderer

## Usage Example
```clojure
;; Creating a new Cat Engine block
(def cat-engine (core/create))

;; Linking to wireless network
(when-let [nodes (wireless-helper/get-nodes-in-range world pos)]
  (wireless-helper/link-generator! tile-entity (rand-nth nodes)))

;; Monitoring energy generation
(let [energy (tile-engine/get-energy engine)
      gen-rate (tile-engine/get-this-tick-gen engine)]
  (println "Current Energy:" energy)
  (println "Generation Rate:" gen-rate))
```

## Best Practices
1. Always check for remote world before processing block activation
2. Use wireless-helper functions for network operations
3. Maintain state consistency through proper serialization
4. Update GUI elements only when necessary
