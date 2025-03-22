# Energy System Architecture

This document describes the architecture and components of the Academy Mod's energy system.

## Overview

The energy system provides a framework for generating, storing, transferring, and consuming energy within the mod. It is built on a network-based architecture where energy nodes can connect to form networks that efficiently distribute energy.

## Core Components

### 1. Network System

The network system manages energy distribution between connected nodes. It consists of the following components:

- **Network Core**: Provides fundamental network operations and data structures
- **Network State**: Manages the state of all networks and nodes
- **Network Handler**: Processes messages and events related to networks
- **Network Optimization**: Implements algorithms for efficient energy routing

### 2. Energy Nodes

Energy nodes are the building blocks of the energy system. They represent physical blocks in the game that can:

- Generate energy
- Store energy
- Transfer energy
- Consume energy

### 3. Security System

A unified security system that handles:

- Access control for nodes and networks
- Rate limiting for connection attempts
- Player blacklisting
- Password protection

### 4. Persistence Framework

The persistence framework saves and loads all system state across game sessions:

- Network configurations
- Node energy levels
- Security settings
- Analytics data
- Monitoring information

### 5. Analytics and Monitoring

- **Analytics**: Collects and analyzes statistics about energy networks
- **Monitoring**: Tracks system health and detects issues

### 6. Configuration

A centralized configuration system that manages all energy system settings.

### 7. API Module

The API module (`api.clj`) provides a clean, stable interface for other mods to interact with the energy system. Key features include:

- Energy storage creation and management
- Energy transfer between tile entities
- Network node discovery and management
- Configuration access
- Energy capability protocol implementation

The API is designed to be backward compatible and provides clear documentation for all public functions.

## Initialization Sequence

The energy system initializes in the following order:

1. Load configuration
2. Initialize registry
3. Initialize network components
4. Initialize security system
5. Initialize analytics
6. Initialize monitoring
7. Initialize persistence
8. Initialize block systems
9. Register event handlers
10. Initialize admin interface

## Dependencies

The energy system has dependencies on the following core systems:
- Event system
- Scheduler system
- NBT persistence
- World management

## Component Interactions

```
                 ┌─────────────┐
                 │   Config    │
                 └──────┬──────┘
                        │
                        ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  Security   │◄───┤    Core     ├───►│ Persistence │
└──────┬──────┘    └──────┬──────┘    └──────┬──────┘
       │                  │                   │
       ▼                  ▼                   ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Nodes     │◄───┤   Network   │    │  Analytics  │
└──────┬──────┘    └──────┬──────┘    └──────┬──────┘
       │                  │                   │
       └──────────┐      │      ┌────────────┘
                  ▼      ▼      ▼
              ┌─────────────────────┐
              │        API         │
              └─────────────────────┘
```

## Usage Examples

### Creating Energy Storage
```clojure
(require '[cn.academy.tech-system.energy-system.api :as energy])

(def storage (energy/create-energy-storage
              :capacity 50000
              :max-receive 1000
              :max-extract 1000))
```

### Finding and Transferring Energy
```clojure
(let [receivers (energy/find-energy-receivers world pos 16)
      target (first receivers)]
  (energy/transfer-energy source target 1000))
```

### Network Management
```clojure
(let [network-id (energy/create-network!)]
  (energy/join-network! node-id network-id)
  (energy/get-network-nodes network-id))
```

## State Management

All stateful components implement the `IPersistable` protocol to ensure consistent state saving and loading.