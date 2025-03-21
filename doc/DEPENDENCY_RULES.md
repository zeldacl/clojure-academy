# Project Dependency Rules

## System Layer Structure

```
[cn.academy.tech-system.energy-system]   <-- Core Energy Infrastructure
         ^        ^        ^
         |        |        |
[cn.academy.blocks.block-node]           <-- Block-specific Implementation
```

## Fundamental Rules

1. Core Energy System (tech-system.energy-system)
   - Acts as foundational infrastructure
   - Provides core protocols and interfaces for energy handling
   - Must not depend on specific block implementations
   - Exports:
     - Network optimization protocols
     - Energy transfer protocols
     - Node registration interfaces
     - Core energy capabilities

2. Block Node System (blocks.block-node)
   - Implements block-specific energy behaviors
   - MUST depend only on energy-system's public APIs
   - Provides concrete implementations of energy nodes
   - Cannot be referenced by energy-system

## Dependency Flow

### Correct Flow Pattern
```
[cn.academy.tech-system.energy-system]
  - Core protocols (IEnergyCapability, INetworkOptimizer)
  - Registration interfaces
  - Network handling
           ↑
           |
[cn.academy.blocks.block-node]
  - Block-specific node implementations
  - Concrete energy storage implementations
  - Network message handlers
```

### Configuration Management

1. Node Configuration Flow:
   ```
   [blocks.block-node]
   Defines node configurations -> Uses energy-system registration API -> [tech-system.energy-system]
   ```

2. Network Message Flow:
   ```
   [tech-system.energy-system]
   Defines protocol -> Implements handlers <- [blocks.block-node]
   ```

## Implementation Guidelines

1. Energy System API Usage
   ```clojure
   ;; In blocks.block-node
   (require '[cn.academy.tech-system.energy-system.api :as energy-api])
   
   ;; Correct: Implementing energy system protocols
   (defrecord BlockNode [config]
     energy-api/IEnergyCapability
     (get-stored-energy [this] ...)
     (get-max-energy [this] ...))
   ```

2. Block Node Implementation
   ```clojure
   ;; In blocks.block-node
   (require '[cn.academy.tech-system.energy-system.network :as network])
   
   ;; Correct: Using energy system's network functionality
   (defn handle-block-update! [block]
     (network/update-node! block))
   ```

## Invalid Patterns

1. Incorrect Dependency Direction:
   ```clojure
   ;; WRONG: Energy system should not depend on block implementations
   (ns cn.academy.tech-system.energy-system.core
     (:require [cn.academy.blocks.block-node.config :as node-config]))
   ```

2. Direct Block Access from Energy System:
   ```clojure
   ;; WRONG: Energy system should not know about specific block types
   (defn process-node [node]
     (when (instance? BlockNode node)
       ...))
   ```

## Benefits of This Structure

1. Modularity
   - Energy system remains implementation-agnostic
   - Block nodes can be modified without affecting core energy logic
   - Multiple node types can coexist

2. Testing
   - Energy system can be tested with mock implementations
   - Block node implementations can be tested in isolation
   - Clear boundaries make integration testing simpler

3. Maintainability
   - Changes to block implementations don't affect energy core
   - Energy system upgrades are isolated from block logic
   - Clear dependency flow simplifies debugging

## System Initialization Order

1. Energy System First
   ```clojure
   (tech-system.energy-system/init!)
   ;; Sets up core protocols and registries
   ```

2. Block Node System Second
   ```clojure
   (blocks.block-node/init!)
   ;; Registers implementations with energy system
   ```

## Migration Guidelines

When adding new functionality:

1. First determine if it belongs in:
   - Energy System: If it's core energy handling logic
   - Block Node: If it's block-specific behavior

2. Follow the dependency rules:
   - New energy features go into energy-system
   - New block features go into blocks.block-node
   - Block features must use energy-system's public API

3. Use proper abstraction:
   - Define protocols in energy-system
   - Implement protocols in blocks.block-node
   - Never create circular dependencies

## Dependency Verification

### Verification Steps

1. Check Namespace Declarations
   ```clojure
   ;; VALID: Block node depending on energy system
   (ns cn.academy.blocks.block-node.core
     (:require [cn.academy.tech-system.energy-system.api :as energy-api]))
   
   ;; INVALID: Energy system depending on block node
   (ns cn.academy.tech-system.energy-system.core
     (:require [cn.academy.blocks.block-node.config :as node-config]))
   ```

2. Check Initialization Order
   ```clojure
   ;; Correct order in project.clj or equivalent
   :init-ns [
     cn.academy.tech-system.energy-system
     cn.academy.blocks.block-node
   ]
   ```

3. Configuration Flow Check
   - Block configurations should be defined in block-node
   - Energy system should provide registration API
   - Block-node should register during initialization

### Common Issues to Check

1. Import Cycles
   - No circular dependencies between packages
   - No mutual dependencies between energy and block systems
   
2. Configuration Sources
   - Node types defined in block-node
   - Energy system only knows about registered types
   - No hardcoded block-specific logic in energy system

3. Interface Dependencies
   - Energy system defines interfaces
   - Block system implements interfaces
   - No implementation details leak upward

### Automatic Verification

Add these checks to your build process:

```clojure
(defn verify-dependencies []
  (let [energy-system-ns (find-namespaces 'cn.academy.tech-system.energy-system)
        block-node-ns (find-namespaces 'cn.academy.blocks.block-node)]
    
    ;; Check for invalid imports
    (doseq [ns energy-system-ns]
      (when (has-import? ns 'cn.academy.blocks.block-node)
        (throw (Exception. "Invalid dependency: energy-system cannot depend on block-node"))))
    
    ;; Verify interface usage
    (doseq [ns block-node-ns]
      (when-not (implements-interfaces? ns ['cn.academy.tech-system.energy-system.api])
        (println "Warning: Block node namespace should implement energy system interfaces")))))
```

## Tools and Scripts

1. Dependency Graph Generator
   ```bash
   lein deps :tree-data-file "deps.dot"
   dot -Tpng deps.dot -o deps.png
   ```

2. Cycle Detection
   ```bash
   lein check-cycles
   ```

3. Interface Compliance Check
   ```bash
   lein check-interfaces
   ```

Remember to run these checks:
- Before merging changes
- During continuous integration
- When refactoring dependencies