# Protocol Specification

This document defines the protocol system used in Academy Craft and the guarantees each protocol provides.

## Core Principles

1. **Protocol Independence**: Each protocol is independent and should not rely on implementation details of other protocols
2. **Implementation Agnostic**: Protocols define behavior without assuming specific Minecraft/Forge implementation details
3. **Versioning Compatible**: Protocol definitions must support multiple Minecraft versions through appropriate abstractions
4. **Testable**: All protocols must be implementable with mock objects for testing
5. **Thread Safety**: Protocol implementations must be thread-safe where applicable

## Protocol Definitions

### IBlock Protocol

Defines block behavior and interaction.

```clojure
(defprotocol IBlock
  (get-properties [this])
  (on-placed [this world pos placer])
  (on-broken [this world pos])
  (on-activated [this world pos player hand]))
```

**Guarantees**:
- Properties are immutable after block registration
- Event methods called in order: placed -> activated -> broken
- World and position arguments are valid when provided
- Handler methods must not modify world state outside their scope

### IEnergyStorage Protocol

Manages energy storage and transfer.

```clojure
(defprotocol IEnergyStorage
  (receive-energy [this amount simulate])
  (extract-energy [this amount simulate])
  (get-energy-stored [this])
  (get-max-energy-stored [this])
  (can-receive? [this])
  (can-extract? [this]))
```

**Guarantees**:
- Energy values are always non-negative
- Current energy never exceeds maximum
- Simulate mode must not modify state
- Thread-safe energy operations
- Energy transfers are atomic

### IWirelessNode Protocol

Handles wireless network connections.

```clojure
(defprotocol IWirelessNode
  (get-range [this])
  (get-max-connections [this])
  (connect [this other])
  (disconnect [this other])
  (can-connect? [this other]))
```

**Guarantees**:
- Connection count never exceeds maximum
- Range checks enforced for connections
- Bidirectional connections maintained
- Thread-safe connection operations
- Connection state consistency preserved

### INetwork Protocol

Manages network message handling.

```clojure
(defprotocol INetwork
  (register-message [this channel message encoder decoder handler direction])
  (send-to-server [this channel message])
  (send-to-client [this channel message player]))
```

**Guarantees**:
- Messages delivered in order sent
- Handler executed on appropriate thread
- Channel registration is thread-safe
- Message size limits enforced
- Invalid messages safely rejected

### IEventBus Protocol

Provides event dispatch system.

```clojure
(defprotocol IEventBus
  (post-event [this event])
  (register-handler [this handler])
  (unregister-handler [this handler]))
```

**Guarantees**:
- Events delivered to all registered handlers
- Handler registration is thread-safe
- Event order preserved
- Exceptions in handlers contained
- No handler state sharing

### IGui Protocol

Defines GUI behavior.

```clojure
(defprotocol IGui
  (init [this])
  (render [this state])
  (handle-input [this input]))
```

**Guarantees**:
- Rendering occurs on client thread
- Input handled in order received
- State updates are atomic
- Resources properly managed
- Thread-safe state access

## Implementation Requirements

1. All implementations must maintain these guarantees
2. Implementation-specific features must not break protocol contracts
3. Adapters must properly bridge protocol and platform behavior
4. Error handling must preserve system stability
5. Performance impact should be minimized

## Testing Requirements

1. Each protocol requires unit tests with mock implementations
2. Integration tests must verify adapter implementations
3. Performance tests for critical protocols
4. Thread safety tests where applicable
5. Error condition testing