# Component Dependencies in the Protocol-Based Architecture

## Overview

This document outlines the proper dependency architecture for the Clojure Academy mod components, particularly focusing on the relationship between block implementations and the energy system.

## Core Principles

1. **Dependency Direction**: Dependencies should flow from concrete implementations to abstractions, not between concrete implementations.
2. **API Boundaries**: Components should only interact through well-defined APIs, not internal implementation details.
3. **Shared Core**: Components can share dependencies on core libraries and protocols, but not on each other's implementations.

## Component Structure

```
                  +----------------+
                  |  mcmod         |
                  |  (protocols)   |
                  +-------^--------+
                          |
             +------------+-------------+
             |                          |
    +--------v---------+      +---------v--------+
    | tech_system      |      |                  |
    | energy_system    <------+ block components |
    | (API)            |      | (implementations)|
    +------------------+      +------------------+
```

### Key Components and Their Dependencies

1. **mcmod**: Contains core protocols and interfaces that define behavior without implementation details.
   * No dependencies on other components
   * Defines `IEnergyStorage`, `IEnergyNetwork`, etc.

2. **tech_system/energy_system**: Implements energy management functionality
   * Depends on mcmod protocols
   * Provides a public API for block components to use
   * Internal implementation details are private to the module

3. **blocks/block_matrix**: Implements a wireless energy matrix block
   * Depends on mcmod protocols
   * Depends on tech_system/energy_system API (not implementation details)
   * No dependencies on other block implementations (like block_node)

4. **blocks/block_node**: Implements wireless energy node blocks
   * Depends on mcmod protocols
   * Depends on tech_system/energy_system API (not implementation details)
   * No dependencies on other block implementations (like block_matrix)

## Proper Dependency Patterns

### ✅ Correct: Using the Energy System API

```clojure
(ns cn.academy.blocks.block-component
  (:require [mcmod.protocols :refer :all]
            [cn.academy.tech-system.energy-system.api :as energy-api]))

(defn register-with-energy-system! [node-type properties]
  (energy-api/register-energy-node-type! node-type properties))
```

### ❌ Incorrect: Reaching into Implementation Details

```clojure
(ns cn.academy.blocks.block-component
  (:require [mcmod.protocols :refer :all]
            ; Don't do this - internal implementation detail
            [cn.academy.tech-system.energy-system.network.distribution :as distribution]))

(defn get-network-details [network-id]
  (distribution/get-network network-id)) ; Accessing internal implementation
```

## Common API Functions

All components should interact with the energy system using the following API functions:

| Function | Purpose |
|----------|---------|
| `energy-api/create-energy-storage` | Create a standard energy storage instance |
| `energy-api/get-network` | Get a network by ID |
| `energy-api/get-network-nodes` | Get nodes for a network |
| `energy-api/join-network!` | Join a node to a network |
| `energy-api/leave-network!` | Remove a node from a network |
| `energy-api/register-energy-node-type!` | Register an energy node type |

## Resolving Dependency Conflicts

If you encounter a dependency conflict between components:

1. Identify any direct dependencies between block implementations
2. Move shared functionality to the energy system API
3. Update components to use the energy system API
4. Remove direct dependencies between block implementations

## Testing Component Dependencies

You can verify proper dependencies using these commands:

```bash
# Check for improper imports between block components
grep -r "cn.academy.blocks.block_matrix" acmod/src/main/clojure/cn/academy/blocks/block_node/
grep -r "cn.academy.blocks.block_node" acmod/src/main/clojure/cn/academy/blocks/block_matrix/

# Check for imports of energy system implementation details
grep -r "cn.academy.tech-system.energy-system.network" acmod/src/main/clojure/cn/academy/blocks/
```