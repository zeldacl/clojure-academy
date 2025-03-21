# Project Dependency Rules

## Core Dependencies Structure

```
cn.academy.tech-system.energy-system  <-- Base system
            ^
            |
cn.academy.blocks.block-node         <-- Implementation
```

## Key Rules

1. Block Node Dependencies
   - block-node DEPENDS ON energy-system
   - block-node cannot be required by energy-system
   - block-node must register its configs through energy-system's public API

2. Energy System Rules
   - Must provide registration APIs for node types
   - Must not have direct dependencies on implementation modules
   - Maintains its own state independent of implementations

3. Configuration Flow
   ```
   [block-node/config.clj] -- defines --> [node type configurations]
                                              |
                                              v
   [block-node/registry.clj] -- registers --> [energy-system/registry.clj]
   ```

4. Runtime Initialization Order
   1. Energy system initializes first
   2. Block node system initializes after
   3. Block node registers its configurations with energy system
   4. Energy system handles all node operations using registered configs

## Correct Usage Examples

### Energy System Registry (energy-system/registry.clj)
```clojure
;; Provides registration API
(defn register-node-type! [node-type config] ...)
(defn get-node-type-config [node-type] ...)
```

### Block Node Registry (blocks/block-node/registry.clj)
```clojure
;; Uses energy system's API
(defn register! [mod-id]
  ;; Register configurations with energy system
  (register-node-types!)
  ;; Register block implementations
  ...)
```

## Incorrect Patterns to Avoid

1. Direct Energy System Dependencies on Block Node:
   ```clojure
   ;; WRONG
   (ns cn.academy.tech-system.energy-system.registry
     (:require [cn.academy.blocks.block-node.config :as node-config]))
   ```

2. Bypassing Registration API:
   ```clojure
   ;; WRONG
   (def node-types @energy-system.registry/node-types-registry)
   ```

## Benefits

1. Clear Separation of Concerns
   - Energy system remains implementation-agnostic
   - Block node can be modified without affecting energy system
   - Multiple node implementations can coexist

2. Testability
   - Energy system can be tested independently
   - Mock node implementations can be registered for testing

3. Maintainability
   - Changes to block node won't break energy system
   - Energy system upgrades won't require block node changes
   - Clear dependency flow makes debugging easier