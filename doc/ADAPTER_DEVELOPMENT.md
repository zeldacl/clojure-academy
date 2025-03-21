# Adapter Development Guidelines

## Protocol-Based Architecture

The Clojure Academy mod follows a strict protocol-based architecture that separates core game logic from Minecraft/Forge implementation details. This document outlines the key principles and rules to follow when developing for this project.

## Fundamental Rule: No Direct Minecraft Imports in acmod

**The `acmod` project must never directly import Minecraft or Forge classes.** Instead, it should only interact with Minecraft through the protocol abstractions defined in the `mcmod` project.

### Correct Example:

```clojure
;; CORRECT: Using protocols
(ns cn.academy.blocks.example
  (:require [mcmod.protocols :refer :all]
            [mcmod.resources :as resources]
            [mcmod.materials :as materials]))

(def texture (resources/create-resource-location "academy" "textures/blocks/example.png"))

(defrecord ExampleBlock []
  IBlock
  (get-properties [_]
    {:material (materials/get-material :iron)
     :hardness 3.0}))
```

### Incorrect Example:

```clojure
;; INCORRECT: Direct Minecraft imports
(ns cn.academy.blocks.example
  (:import [net.minecraft.util ResourceLocation]
           [net.minecraft.block.material Material]))
           
(def texture (ResourceLocation. "academy" "textures/blocks/example.png"))

(defrecord ExampleBlock []
  (get-properties [_]
    {:material Material/IRON
     :hardness 3.0}))
```

## Common Replacements

Here are the most common Minecraft classes and their protocol-based replacements:

| Minecraft Class         | Protocol Replacement                                |
|-------------------------|-----------------------------------------------------|
| `ResourceLocation`      | `mcmod.resources/create-resource-location`          |
| `Material`              | `mcmod.materials/get-material`                      |
| `EntityPlayer`          | `mcmod.player` protocol functions                   |
| `BlockPos`              | Use maps with `:x`, `:y`, `:z` keys                 |
| `TileEntity`            | Implement `mcmod.protocols/ITileEntity` protocol    |
| `Container`             | Implement `mcmod.protocols/IContainer` protocol     |
| `ItemStack`             | Implement `mcmod.protocols/IItemStack` protocol     |

## Benefits of Protocol-Based Architecture

1. **Version Independence**: Core logic works across different Minecraft versions
2. **Testability**: Core logic can be tested without a Minecraft environment
3. **Cleaner Separation**: Clear boundaries between game logic and implementation
4. **Easier Maintenance**: Changes to Minecraft APIs don't require changes to core logic
5. **Simpler Debugging**: Issues are isolated to either core logic or adapters

## Implementation Details

The protocol abstractions are defined in `mcmod` and implemented by version-specific adapters in `forge_1_12_2`, `forge_1_15_2`, and `forge_1_16_5` projects. This allows the same core logic to run on multiple Minecraft versions with minimal changes.

When you need functionality that doesn't have an existing protocol, follow this process:

1. Define a new protocol in `mcmod` 
2. Implement the protocol in the appropriate Forge adapter
3. Use the protocol in `acmod` instead of direct Minecraft imports

# Protocol Adapter Development Guide

This guide explains how to implement protocol adapters for new Minecraft versions or platforms.

## Overview

Protocol adapters bridge the gap between our protocol-based mod architecture and specific Minecraft/platform implementations. A good adapter:

1. Maintains protocol guarantees
2. Handles platform-specific details
3. Provides clean error handling
4. Ensures thread safety
5. Minimizes performance overhead

## Adapter Implementation Process

### 1. Study Protocol Requirements

First, thoroughly understand the protocol you're implementing:
- Review PROTOCOL_SPEC.md
- Study protocol unit tests
- Note all guarantees that must be maintained

### 2. Research Platform API

For your target platform:
- Identify relevant platform APIs
- Understand threading model
- Note platform-specific limitations
- Research common patterns

### 3. Design Adapter Structure

Create an adapter design that:
- Implements protocol fully
- Handles platform specifics
- Maintains thread safety
- Provides error recovery
- Optimizes performance

### 4. Implementation Steps

1. Create adapter class implementing protocol
2. Add platform-specific fields
3. Implement protocol methods
4. Add error handling
5. Ensure thread safety
6. Add performance optimizations
7. Write integration tests

## Example: Block Protocol Adapter

Here's an example adapter implementation:

```clojure
(defrecord ForgeBlockAdapter [block-instance properties]
  IBlock
  (get-properties [_]
    properties)
    
  (on-placed [_ world pos placer]
    ;; Handle thread safety
    (when (world/is-thread-safe? world)
      (try
        (.onBlockPlaced block-instance world pos placer)
        (catch Exception e
          (log/error e "Error in block placement")))))
          
  (on-broken [_ world pos]
    (when (world/is-thread-safe? world)
      (try
        (.onBlockBroken block-instance world pos)
        (catch Exception e
          (log/error e "Error in block broken")))))
          
  (on-activated [_ world pos player hand]
    (when (world/is-thread-safe? world)
      (try
        (.onBlockActivated block-instance world pos player hand)
        (catch Exception e
          (log/error e "Error in block activation"))))))
```

## Common Adapter Patterns

### Thread Safety

```clojure
(defn with-thread-safety [world f]
  (when (world/is-thread-safe? world)
    (try 
      (f)
      (catch Exception e
        (log/error e "Operation failed")))))
```

### Error Handling

```clojure
(defn safely [f]
  (try
    (f)
    (catch Exception e
      (log/error e "Operation failed")
      nil)))
```

### State Management

```clojure
(defrecord StateManager [state]
  (get-state [this]
    @state)
    
  (update-state [this f]
    (swap! state f))
    
  (with-state-lock [this f]
    (locking state
      (f @state))))
```

## Testing Adapters

### Integration Tests

```clojure
(deftest test-block-adapter
  (let [adapter (->ForgeBlockAdapter block props)]
    
    (testing "Thread safety"
      (let [world (create-test-world)]
        (is (nil? (on-placed adapter 
                            (unsafe-world)
                            [0 0 0]
                            nil)))))
                            
    (testing "Error handling"
      (let [world (create-test-world)]
        (is (nil? (on-activated adapter
                               world
                               [0 0 0]
                               nil
                               nil)))))))
```

### Performance Tests

```clojure
(deftest test-adapter-performance
  (let [adapter (->ForgeBlockAdapter block props)
        world (create-test-world)]
        
    (testing "Operation timing"
      (let [start (System/nanoTime)
            _ (dotimes [_ 1000]
                (on-activated adapter world [0 0 0] nil nil))
            end (System/nanoTime)
            avg (/ (- end start) 1000)]
        (is (< avg 1000000))))))
```

## Best Practices

1. Keep adapters focused and simple
2. Handle all error cases
3. Log meaningful errors
4. Test thread safety thoroughly
5. Measure performance impact
6. Document platform specifics
7. Write integration tests

## Common Issues

1. **Thread Safety Violations**
   - Always check thread safety
   - Use appropriate synchronization
   - Document thread requirements

2. **Memory Leaks**
   - Clean up resources properly
   - Use weak references when needed
   - Test memory usage

3. **Performance Problems**
   - Profile adapter operations
   - Minimize object creation
   - Cache when appropriate

4. **Error Handling**
   - Never swallow exceptions
   - Log with context
   - Maintain system stability

## Additional Resources

- Protocol Specification (PROTOCOL_SPEC.md)
- Test Suite Documentation
- Platform API Documentation
- Development Environment Setup