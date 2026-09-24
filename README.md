# SQS (Sports Queue System)

SQS is a Java Swing desktop application for managing player check-in, queue formation, court assignment, and live match tracking for a sports facility.

## Features

### Queue workflow

- Adds players by name, match format, and skill level.
- Tracks players in `OH_HOLD`, `IN_QUEUE`, and `IN_MATCH` states.
- Supports both `SINGLE` and `DOUBLE` match formats.
- Enforces a maximum player count of 2 for singles and 4 for doubles.
- Automatically groups compatible players into match queues using skill matching rules.
- Allows manual creation of a match from selected players.
- Sends players back to the on-hold list when a match is completed.

### Match-making and scheduling

- Manages up to four courts in a single system.
- Matches players by format and skill compatibility.
- Checks compatibility using a skill difference threshold of 1 rank.
- Creates a new pending match when a player cannot join an existing compatible match.
- Marks a match as ready when it reaches full capacity.
- Tracks queue and court status across the application.

### Court and match management

- Starts, pauses, ends, and clears matches on courts.
- Shows each court's current match, players, and timer countdown.
- Refreshes court timers every second while matches are running.
- Frees courts automatically when a match finishes.
- Allows queue and on-hold lists to be cleared or adjusted from the GUI.

### Player and match status tracking

- Tracks players using `PlayerStatus` values:
  - `OH_HOLD`
  - `IN_QUEUE`
  - `IN_MATCH`
- Tracks matches using `MatchStatus` values:
  - `PENDING`
  - `RUNNING`
  - `PAUSED`
  - `FINISHED`
- Stores match details including court assignment, format, player list, and score state.

## Project Structure

- `src/` - Java source files
- `src/GUI/MainGUI.java` - Main Swing interface and application entry point
- `src/GUI/OnHoldPlayerCard.java` - On-hold player display card
- `src/GUI/PlayerRowPanel.java` - Player row component in the UI
- `src/GUI/QueueMatchCard.java` - Queue match display card
- `src/Collection/OnHoldList.java` - Player waiting list storage
- `src/Collection/QueueList.java` - Pending and ready match queue storage
- `src/Management/QueueService.java` - Queue lifecycle and player-to-match operations
- `src/Management/MatchMaker.java` - Compatibility and match creation logic
- `src/Management/CourtManager.java` - Court assignment and match control
- `src/Model/Player.java` - Player identity, format, status, and skill
- `src/Model/Match.java` - Match data, timer, scoring, and lifecycle
- `src/Model/Court.java` - Court state and assignment tracking
- `src/Model/MatchFormat.java` - Supported match formats
- `src/Model/MatchStatus.java` - Match lifecycle states
- `src/Model/PlayerStatus.java` - Player lifecycle states
- `src/Model/SkillLevel.java` - Skill ranking values
- `build.bat` - Compiles the project and packages the Windows app
- `build/` - Compiler output generated during the build
- `dist/` - Generated desktop application output

## Requirements

- Windows
- JDK 14 or newer with `javac`, `jar`, and `jpackage` available on `PATH`

## Build the Windows Application

From the `SQS` directory, run:

```bat
build.bat
```

The script compiles the source files, creates `build/package-input/SQS.jar`, and generates a Windows executable in:

```text
dist/SQS/SQS.exe
```

## Run from Compiled Classes

After building the project, you can launch the application with:

```bat
java -cp build/classes GUI.MainGUI
```

## Queue Rules

- A single match can include up to 2 players.
- A doubles match can include up to 4 players.
- Players are only matched when their format is the same.
- Players are considered compatible when skill difference is within 1 rank.
- A player in the on-hold list can be moved to the queue manually or automatically.
- Completed matches return all players to the on-hold list.
- Courts can only hold one active match at a time.

## Branch Naming

Examples:

- `feature/player-queue`
- `ui-court-management`
- `feature/match-making`

## Contributors

- **Perez, Ronin** | Team Head | Build/ Collection/ Model/ Management/ GUI/
- **Santarin, Francos** | Front end | GUI/
- **Andres, Jerald** | Back end 
- **Bagagnan, Aldrin** | Back end | GUI/
- **Ebonia, Tristan** | Front end 
- **Flores, Ryiogi Zen** | Back end | Collection/ Model/ Management/
- **Lagoras, Rus** | Fullstack 

