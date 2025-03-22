# Block Node Architecture and Optimization

## Overview

This document describes the architecture of the `block_node` module and the optimizations applied to improve code organization and reduce duplication. The wireless node system provides configurable energy storage and transfer capabilities that work with the energy system.

## Module Structure

The `block_node` module is composed of the following components:

```
block_node/
  ├── config.clj        - Node type configurations and properties
  ├── container.clj     - Container implementation for inventory interaction
  ├── core.clj          - Core block implementation and capability providers
  ├── gui.clj           - GUI implementation for node configuration
  ├── init.clj          - Initialization and registration
  ├── network.clj       - Network message handling for synchronization
  ├── registry.clj      - Block and tile entity registration
  ├── tile.clj          - Tile entity implementation with energy handling
  └── ui-components.clj - Shared UI functionality for GUI and containers
```

## Key Optimizations

### 1. Shared UI Component Extraction

Created a `ui-components.clj` module that centralizes common GUI and container functionality:
- Shared UI constants (dimensions, textures)
- Standardized property access for tile entities
- Unified error handling and logging
- Helper macros for Java interop

**Benefits:**
- Reduced code duplication between GUI and container implementations
- Consistent error handling and property access
- Easier maintenance when UI requirements change

### 2. Data-Driven Registration

Updated the registry system to use a map-driven approach:
- Centralized node type configurations in `config.clj`
- Used a structured map for registration in `registry.clj`
- Removed redundant registration code

**Benefits:**
- Adding new node types requires minimal code changes
- Registration process is more maintainable
- Reduced likelihood of inconsistencies

### 3. Improved Tile Entity Implementation

Enhanced the tile entity module with better organization:
- Helper functions for grouped functionality (properties, state, NBT)
- Standard energy interface implementation
- Consistent utility functions
- Better error handling

**Benefits:**
- More maintainable code structure
- Clearer separation of concerns
- Better error resilience

### 4. Standardized Message Handling

Improved network message handling with common utilities:
- Shared message validation and error handling
- Centralized message type registration
- Added convenience functions for sending messages

**Benefits:**
- More robust error handling
- Reduced code duplication in message processing
- Easier to add new message types

## Dependency Structure

The `block_node` module follows the project's dependency guidelines:

1. Depends directly on `mcmod.protocols` for core interfaces
2. Depends on `tech-system.energy-system.api` for energy functionality
3. Does not depend on any other block implementations
4. Exposes a clean API for other modules to interact with nodes

## Usage Examples

### Node Registration

```clojure
;; Register all node types with the mod
(require '[cn.academy.blocks.block-node.registry :as registry])
(registry/register! "mymod")
```

### Energy Interaction

```clojure
;; Access node energy capabilities
(require '[cn.academy.blocks.block-node.tile :as node])
(let [tile-entity (get-tile-entity world pos)]
  (when (node/node? tile-entity)
    (println "Energy stored:" (node/get-energy tile-entity))
    (node/charge tile-entity 1000 false)))
```

### Network Communication

```clojure
;; Send a state update
(require '[cn.academy.blocks.block-node.network :as network])
(network/send-state-update! network-instance pos true)
```

## Best Practices

When working with the `block_node` module:

1. Use the constants in `config.clj` for type-safe access to node types and properties
2. Access tile entities through the utility functions in `tile.clj`
3. Use the `ui-components` module for consistent UI element handling
4. Register messages with the centralized `register-messages!` function
5. Use the data-driven approach when extending functionality